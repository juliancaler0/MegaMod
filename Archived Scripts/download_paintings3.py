"""
Download paintings using Wikimedia API to get proper thumbnail URLs,
with 30-second delays between each request.
"""
import os, time, urllib.request, io, json
from PIL import Image

BASE = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures"
PAINTING_DIR = os.path.join(BASE, "painting")
ITEM_DIR = os.path.join(BASE, "item")

# Map painting ID to Wikimedia Commons filename and target size
PAINTINGS = [
    ("garden_of_earthly_delights", "The_Garden_of_earthly_delights.jpg", 256, 128),
    ("the_milkmaid", "Johannes_Vermeer_-_Het_melkmeisje_-_Google_Art_Project.jpg", 128, 128),
    ("napoleon_crossing_alps", "David_-_Napoleon_crossing_the_Alps_-_Malmaison2.jpg", 128, 192),
    ("raft_of_the_medusa", "JEAN_LOUIS_TH%C3%89ODORE_G%C3%89RICAULT_-_La_Balsa_de_la_Medusa_(Museo_del_Louvre,_1818-19).jpg", 256, 192),
    ("the_hay_wain", "John_Constable_-_The_Hay_Wain_(1821).jpg", 256, 128),
    ("rain_steam_speed", "Rain_Steam_and_Speed_the_Great_Western_Railway.jpg", 128, 128),
    ("bar_at_folies_bergere", "Edouard_Manet,_A_Bar_at_the_Folies-Berg%C3%A8re.jpg", 256, 128),
    ("the_card_players", "Paul_C%C3%A9zanne_-_Les_Joueurs_de_cartes.jpg", 128, 128),
    ("the_old_guitarist", "Old_guitarist_chicago.jpg", 64, 128),
    ("the_dream_rousseau", "Henri_Rousseau_-_Il_sogno.jpg", 256, 128),
    ("tower_of_babel", "Pieter_Bruegel_the_Elder_-_The_Tower_of_Babel_(Vienna)_-_Google_Art_Project_-_edited.jpg", 128, 128),
    ("christinas_world", "Christinasworld.jpg", 256, 128),
    ("the_two_fridas", "The_Two_Fridas.jpg", 128, 128),
    ("broadway_boogie_woogie", "Piet_Mondriaan,_1942_-_Broadway_Boogie_Woogie.jpg", 128, 128),
    ("composition_viii", "Vassily_Kandinsky,_1923_-_Composition_8,_huile_sur_toile,_140_cm_x_201_cm,_Mus%C3%A9e_Guggenheim,_New_York.jpg", 256, 128),
    ("dogs_playing_poker", "A_Friend_in_Need_1903_C.M.Coolidge.jpg", 256, 128),
    ("lady_of_shalott", "John_William_Waterhouse_-_The_Lady_of_Shalott_-_Google_Art_Project.jpg", 128, 128),
    ("girl_before_mirror", "Picasso_Girl_before_a_Mirror_1932.jpg", 128, 128),
    ("luncheon_boating_party", "Pierre-Auguste_Renoir_-_Luncheon_of_the_Boating_Party_-_Google_Art_Project.jpg", 256, 192),
    ("birth_of_the_world", "Joan_Mir%C3%B3,_1925,_The_Birth_of_the_World.jpg", 128, 128),
    ("a_bigger_splash", "A_Bigger_Splash.jpg", 128, 128),
    ("campbell_soup_cans", "Campbell%27s_Soup_Can_(Tomato)_(1962).jpg", 64, 128),
    ("number_five_1948", "No._5,_1948.jpg", 128, 128),
    ("the_blue_boy", "Thomas_Gainsborough_-_The_Blue_Boy_(The_Huntington_Library,_San_Marino_L._A.).jpg", 64, 128),
    ("treachery_of_images", "MagrittePipe.jpg", 128, 64),
    ("luncheon_on_the_grass", "%C3%89douard_Manet_-_Le_D%C3%A9jeuner_sur_l%27herbe.jpg", 256, 128),
]

def add_frame(img):
    w, h = img.size
    framed = img.copy()
    dark_wood = (42, 26, 14)
    mid_wood = (90, 58, 30)
    gold = (139, 105, 20)
    for x in range(w):
        framed.putpixel((x, 0), dark_wood)
        framed.putpixel((x, h-1), dark_wood)
    for y in range(h):
        framed.putpixel((0, y), dark_wood)
        framed.putpixel((w-1, y), dark_wood)
    for x in range(1, w-1):
        framed.putpixel((x, 1), mid_wood)
        framed.putpixel((x, h-2), mid_wood)
    for y in range(1, h-1):
        framed.putpixel((1, y), mid_wood)
        framed.putpixel((w-1-1, y), mid_wood)
    for cx, cy in [(0,0), (w-1,0), (0,h-1), (w-1,h-1)]:
        framed.putpixel((cx, cy), gold)
        if cx+1 < w: framed.putpixel((cx+1, cy), gold)
        if cx-1 >= 0: framed.putpixel((cx-1, cy), gold)
        if cy+1 < h: framed.putpixel((cx, cy+1), gold)
        if cy-1 >= 0: framed.putpixel((cx, cy-1), gold)
    return framed

def get_image_url_via_api(filename):
    """Use Wikimedia API to get a proper image URL."""
    api_url = f"https://en.wikipedia.org/w/api.php?action=query&titles=File:{filename}&prop=imageinfo&iiprop=url&iiurlwidth=640&format=json"
    req = urllib.request.Request(api_url, headers={
        "User-Agent": "MegaModMuseum/1.0 (https://github.com/megamod; julian@example.com) Python/3.12",
        "Accept": "application/json"
    })
    with urllib.request.urlopen(req, timeout=15) as resp:
        data = json.loads(resp.read().decode())
    pages = data.get("query", {}).get("pages", {})
    for page in pages.values():
        imageinfo = page.get("imageinfo", [{}])
        if imageinfo:
            # Prefer thumburl (resized), fall back to original url
            return imageinfo[0].get("thumburl") or imageinfo[0].get("url")
    return None

total = len(PAINTINGS)
done = 0
failed = []

for i, (pid, wiki_file, tw, th) in enumerate(PAINTINGS):
    painting_path = os.path.join(PAINTING_DIR, f"{pid}.png")
    item_path = os.path.join(ITEM_DIR, f"{pid}.png")

    if os.path.exists(painting_path) and os.path.exists(item_path):
        print(f"[{i+1}/{total}] SKIP {pid}")
        done += 1
        continue

    try:
        # Step 1: Get proper URL via API
        print(f"[{i+1}/{total}] Getting URL for {pid}...", end=" ", flush=True)
        img_url = get_image_url_via_api(wiki_file)
        if not img_url:
            print("FAIL - no URL from API")
            failed.append(pid)
            time.sleep(30)
            continue

        time.sleep(5)  # Small delay between API call and image download

        # Step 2: Download image
        req = urllib.request.Request(img_url, headers={
            "User-Agent": "MegaModMuseum/1.0 (https://github.com/megamod; julian@example.com) Python/3.12"
        })
        with urllib.request.urlopen(req, timeout=20) as response:
            img_data = response.read()

        img = Image.open(io.BytesIO(img_data)).convert("RGB")

        # Step 3: Create painting texture
        painting = img.resize((tw, th), Image.LANCZOS)
        painting = add_frame(painting)
        painting.save(painting_path, "PNG")

        # Step 4: Create item texture
        item = img.resize((32, 32), Image.LANCZOS).convert("RGBA")
        item = add_frame(item)
        item.save(item_path, "PNG")

        print(f"OK ({tw}x{th})")
        done += 1
    except Exception as e:
        print(f"FAIL: {e}")
        failed.append(pid)

    # 30 second delay between each painting
    if i < total - 1:
        print(f"  Waiting 30s...", flush=True)
        time.sleep(30)

print(f"\nComplete: {done}/{total} succeeded")
if failed:
    print(f"Failed: {', '.join(failed)}")
