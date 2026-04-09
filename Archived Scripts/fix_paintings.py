"""Fix undersized paintings and replace No. 5 with Fumée d'Ambre Gris."""
import os, time, json, urllib.request, io, ssl
from PIL import Image

BASE = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures"
VARIANT_DIR = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\data\megamod\painting_variant"
PAINTING_DIR = os.path.join(BASE, "painting")
ITEM_DIR = os.path.join(BASE, "item")

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

def add_frame(img):
    w, h = img.size
    framed = img.copy()
    dark_wood = (42, 26, 14); mid_wood = (90, 58, 30); gold = (139, 105, 20)
    for x in range(w):
        framed.putpixel((x, 0), dark_wood); framed.putpixel((x, h-1), dark_wood)
    for y in range(h):
        framed.putpixel((0, y), dark_wood); framed.putpixel((w-1, y), dark_wood)
    for x in range(1, w-1):
        framed.putpixel((x, 1), mid_wood); framed.putpixel((x, h-2), mid_wood)
    for y in range(1, h-1):
        framed.putpixel((1, y), mid_wood); framed.putpixel((w-1-1, y), mid_wood)
    for cx, cy in [(0,0), (w-1,0), (0,h-1), (w-1,h-1)]:
        framed.putpixel((cx, cy), gold)
        if cx+1 < w: framed.putpixel((cx+1, cy), gold)
        if cx-1 >= 0: framed.putpixel((cx-1, cy), gold)
        if cy+1 < h: framed.putpixel((cx, cy+1), gold)
        if cy-1 >= 0: framed.putpixel((cx, cy-1), gold)
    return framed

def save_both(pid, img, tw, th):
    painting = img.resize((tw, th), Image.LANCZOS)
    painting = add_frame(painting)
    painting.save(os.path.join(PAINTING_DIR, f"{pid}.png"), "PNG")
    item = img.resize((32, 32), Image.LANCZOS).convert("RGBA")
    item = add_frame(item)
    item.save(os.path.join(ITEM_DIR, f"{pid}.png"), "PNG")

def update_variant(pid, w_blocks, h_blocks):
    path = os.path.join(VARIANT_DIR, f"{pid}.json")
    with open(path, 'w') as f:
        f.write(json.dumps({"asset_id": f"megamod:{pid}", "width": w_blocks, "height": h_blocks}))

def try_url(url):
    if url.startswith("//"): url = "https:" + url
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"})
    with urllib.request.urlopen(req, timeout=20, context=ctx) as resp:
        return resp.read()

def search_commons(query):
    url = f"https://commons.wikimedia.org/w/api.php?action=query&list=search&srsearch={urllib.request.quote(query)}&srnamespace=6&format=json"
    req = urllib.request.Request(url, headers={"User-Agent": "MegaModMuseum/1.0 (julian@example.com)"})
    with urllib.request.urlopen(req, timeout=15, context=ctx) as resp:
        data = json.loads(resp.read().decode())
    results = data.get("query", {}).get("search", [])
    for r in results[:3]:
        title = r.get("title", "")
        if title.startswith("File:"):
            info_url = f"https://commons.wikimedia.org/w/api.php?action=query&titles={urllib.request.quote(title)}&prop=imageinfo&iiprop=url&iiurlwidth=800&format=json"
            req2 = urllib.request.Request(info_url, headers={"User-Agent": "MegaModMuseum/1.0 (julian@example.com)"})
            with urllib.request.urlopen(req2, timeout=15, context=ctx) as resp2:
                info = json.loads(resp2.read().decode())
            pages = info.get("query", {}).get("pages", {})
            for page in pages.values():
                ii = page.get("imageinfo", [{}])
                if ii:
                    return ii[0].get("thumburl") or ii[0].get("url")
    return None

# 1. Resize existing undersized paintings (just re-read and resize the existing texture)
RESIZES = {
    "persistence_of_memory": (256, 128, 4, 2),   # was 128x64 (2x1)
    "impression_sunrise": (128, 128, 2, 2),        # was 128x64 (2x1)
    "water_lilies": (256, 128, 4, 2),              # was 128x128 (2x2)
    "the_great_wave": (256, 128, 4, 2),            # was 128x64 (2x1)
    "treachery_of_images": (128, 128, 2, 2),       # was 128x64 (2x1)
}

print("=== Resizing undersized paintings ===")
for pid, (tw, th, bw, bh) in RESIZES.items():
    painting_path = os.path.join(PAINTING_DIR, f"{pid}.png")
    img = Image.open(painting_path).convert("RGB")
    # Strip existing frame (2px border) before resizing
    inner = img.crop((2, 2, img.width - 2, img.height - 2))
    save_both(pid, inner, tw, th)
    update_variant(pid, bw, bh)
    print(f"  {pid}: -> {tw}x{th} ({bw}x{bh} blocks)")

# 2. Replace number_five_1948 with Fumée d'Ambre Gris
print("\n=== Replacing No. 5 with Fumée d'Ambre Gris ===")
try:
    img_url = search_commons("Fumée d'Ambre Gris John Singer Sargent")
    if img_url:
        print(f"  Found: {img_url[:70]}...")
        time.sleep(3)
        data = try_url(img_url)
        img = Image.open(io.BytesIO(data)).convert("RGB")
        # Portrait painting - 2x3 blocks (128x192)
        save_both("number_five_1948", img, 128, 192)
        update_variant("number_five_1948", 2, 3)
        print("  OK (128x192, 2x3 blocks)")
    else:
        print("  Not found via search")
except Exception as e:
    print(f"  FAIL: {e}")

print("\nDone!")
