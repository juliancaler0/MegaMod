package com.ultra.megamod.feature.citizen.blockui;

import com.ultra.megamod.feature.citizen.blockui.controls.*;
import com.ultra.megamod.feature.citizen.blockui.views.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilities to load xml files.
 */
public final class Loader extends SimplePreparableReloadListener<Map<Identifier, PaneParams>>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Loader.class);

    public static final Loader INSTANCE = new Loader();

    private final Map<String, Function<PaneParams, ? extends Pane>> paneFactories = new HashMap<>();

    private Map<Identifier, PaneParams> xmlCache = new HashMap<>();

    private Loader()
    {
        // Register core pane types
        register("view", View::new);

        // Controls
        register("text", Text::new);
        register("button", ButtonImage::new);
        register("buttonimage", ButtonImage::new);
        register("toggle", ToggleButton::new);
        register("image", Image::new);
        register("itemicon", ItemIcon::new);
        register("entityicon", EntityIcon::new);
        register("input", TextFieldVanilla::new);
        register("textfield", TextFieldVanilla::new);
        register("textfieldvanilla", TextFieldVanilla::new);
        register("checkbox", CheckBox::new);

        // Views / Containers
        register("scrollingcontainer", p -> { throw new UnsupportedOperationException("ScrollingContainer is created internally by ScrollingView"); });
        register("list", ScrollingList::new);
        register("scrollinglist", ScrollingList::new);
        register("scrollgroup", ScrollingGroup::new);
        register("scrollinggroup", ScrollingGroup::new);
        register("dropdown", DropDownList::new);
        register("switch", SwitchView::new);
        register("box", Box::new);
        register("group", Group::new);
        register("zoomdragview", ZoomDragView::new);
        register("overlay", OverlayView::new);
    }

    /**
     * registers an element definition class so it can be used in
     * gui definition files
     *
     * @param name          the tag name of the element in the definition file
     * @param factoryMethod the constructor/method to create the element Pane
     */
    public void register(final String name, final Function<PaneParams, ? extends Pane> factoryMethod)
    {
        if (paneFactories.containsKey(name))
        {
            throw new IllegalArgumentException("Duplicate pane type '" + name + "' when registering Pane class method.");
        }

        paneFactories.put(name, factoryMethod);
    }

    /**
     * Uses the loaded parameters to construct a new Pane tree
     *
     * @param params the parameters for the new pane and its children
     * @return the created Pane
     */
    private Pane createFromPaneParams(final PaneParams params)
    {
        final String name = params.getType();
        if (paneFactories.containsKey(name))
        {
            return paneFactories.get(name).apply(params);
        }

        LOGGER.error("There is no factory method for " + name);
        return null;
    }

    /**
     * Create a pane from its xml parameters.
     *
     * @param params xml parameters.
     * @param parent parent view.
     * @return the new pane.
     */
    public static Pane createFromPaneParams(final PaneParams params, final View parent)
    {
        if ("layout".equalsIgnoreCase(params.getType()))
        {
            params.getResource("source", r -> createFromXMLFile(r, parent));
            return null;
        }

        if (parent instanceof final BOWindow window && params.getType().equals("window"))
        {
            window.loadParams(params);
            parent.parseChildren(params);
            return parent;
        }
        else if (parent instanceof View && params.getType().equals("window")) // layout
        {
            parent.parseChildren(params);
            return parent;
        }
        else
        {
            params.setParentView(parent);
            final Pane pane = INSTANCE.createFromPaneParams(params);

            if (pane != null)
            {
                pane.putInside(parent);
                pane.parseChildren(params);
            }
            return pane;
        }
    }

    /**
     * Parse XML contained in an Identifier into contents for a View, returning the root pane.
     * Unlike createFromXMLFile, this is intended for injecting sub-layouts into existing windows.
     *
     * @param resource xml as an {@link Identifier}.
     * @param parent   parent view.
     * @return the root pane created from the XML.
     */
    public static Pane createFromXMLFile2(final Identifier resource, final View parent)
    {
        return createFromXMLFile(resource, parent);
    }

    /**
     * Parse XML contained in an Identifier into contents for a Window.
     *
     * @param resource xml as an {@link Identifier}.
     * @param parent   parent view.
     */
    public static Pane createFromXMLFile(final Identifier resource, final View parent)
    {
        if (INSTANCE.xmlCache.containsKey(resource))
        {
            try
            {
                return createFromPaneParams(INSTANCE.xmlCache.get(resource), parent);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Can't parse xml at: " + resource.toString(), e);
            }
        }
        else
        {
            throw new RuntimeException("Gui at \"" + resource.toString() + "\" was not found!");
            // TODO: create "missing gui" gui and don't crash?
        }
    }

    @Override
    protected Map<Identifier, PaneParams> prepare(final ResourceManager rm, final ProfilerFiller profiler)
    {
        profiler.startTick();
        profiler.push("BlockUI-xml-lookup-parsing");

        final Map<Identifier, PaneParams> foundXmls = new HashMap<>();
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder;
        try
        {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        }
        catch (final ParserConfigurationException e)
        {
            profiler.pop();
            profiler.endTick();
            throw new RuntimeException(e);
        }

        rm.listResources("gui", rl -> rl.getPath().endsWith(".xml")).forEach((rl, res) -> {
            final Document doc;
            try (final InputStream is = res.open())
            {
                doc = documentBuilder.parse(is);
            }
            catch (final IOException | SAXException e)
            {
                LOGGER.error("Failed to load xml at: " + rl.toString(), e);
                return;
            }

            doc.getDocumentElement().normalize();
            foundXmls.put(rl, new PaneParams(doc.getDocumentElement()));
        });

        profiler.pop();
        profiler.endTick();
        return foundXmls;
    }

    @Override
    protected void apply(final Map<Identifier, PaneParams> foundXmls, final ResourceManager rm, final ProfilerFiller profiler)
    {
        xmlCache = foundXmls;
    }
}
