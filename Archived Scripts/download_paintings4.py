"""
Download paintings from alternative sources (not Wikimedia).
Uses multiple fallback sources per painting.
"""
import os, time, urllib.request, io, json, ssl
from PIL import Image

BASE = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures"
PAINTING_DIR = os.path.join(BASE, "painting")
ITEM_DIR = os.path.join(BASE, "item")

# Disable SSL verification for some sources
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

HEADERS = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"}

# Alternative sources: rawpixel CDN, Met Museum API, direct commons (full res, not thumb)
PAINTINGS = [
    ("garden_of_earthly_delights", 256, 128, [
        "https://www.pubhist.com/w/img/a-tour-of-the-garden-of-earthly-delights-by-hieronymus-bosch-46.jpg",
        "https://collectionapi.metmuseum.org/api/collection/v1/iiif/459028/946399/main-image",
    ]),
    ("the_milkmaid", 128, 128, [
        "https://www.rijksmuseum.nl/assets/1b2b1aec-b917-4437-8e82-60685241c7df?w=640",
    ]),
    ("napoleon_crossing_alps", 128, 192, [
        "https://images.metmuseum.org/CRDImages/ep/original/DT1502.jpg",
    ]),
    ("raft_of_the_medusa", 256, 192, [
        "https://images.metmuseum.org/CRDImages/ep/original/DP-26895-001.jpg",
    ]),
    ("the_hay_wain", 256, 128, []),
    ("rain_steam_speed", 128, 128, []),
    ("bar_at_folies_bergere", 256, 128, []),
    ("the_card_players", 128, 128, [
        "https://images.metmuseum.org/CRDImages/ep/original/DP-13787-001.jpg",
    ]),
    ("the_old_guitarist", 64, 128, []),
    ("the_dream_rousseau", 256, 128, []),
    ("tower_of_babel", 128, 128, []),
    ("christinas_world", 256, 128, []),
    ("the_two_fridas", 128, 128, []),
    ("broadway_boogie_woogie", 128, 128, []),
    ("composition_viii", 256, 128, []),
    ("dogs_playing_poker", 256, 128, []),
    ("lady_of_shalott", 128, 128, []),
    ("girl_before_mirror", 128, 128, []),
    ("luncheon_boating_party", 256, 192, []),
    ("birth_of_the_world", 128, 128, []),
    ("a_bigger_splash", 128, 128, []),
    ("campbell_soup_cans", 64, 128, []),
    ("number_five_1948", 128, 128, []),
    ("the_blue_boy", 64, 128, []),
    ("treachery_of_images", 128, 64, []),
    ("luncheon_on_the_grass", 256, 128, [
        "https://images.metmuseum.org/CRDImages/ep/original/DP-14936-001.jpg",
    ]),
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

def try_download(url):
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=20, context=ctx) as resp:
        return resp.read()

def search_met_museum(query):
    """Search The Met Museum API for a painting and return the primary image URL."""
    search_url = f"https://collectionapi.metmuseum.org/public/collection/v1/search?hasImages=true&q={urllib.request.quote(query)}"
    req = urllib.request.Request(search_url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=15, context=ctx) as resp:
        data = json.loads(resp.read().decode())
    object_ids = data.get("objectIDs", [])
    for oid in object_ids[:3]:  # Check first 3 results
        obj_url = f"https://collectionapi.metmuseum.org/public/collection/v1/objects/{oid}"
        req2 = urllib.request.Request(obj_url, headers=HEADERS)
        with urllib.request.urlopen(req2, timeout=15, context=ctx) as resp2:
            obj = json.loads(resp2.read().decode())
        img_url = obj.get("primaryImage", "")
        if img_url and obj.get("isPublicDomain", False):
            return img_url
    return None

# Search terms for Met Museum API
MET_SEARCH = {
    "garden_of_earthly_delights": "Garden Earthly Delights Bosch",
    "the_milkmaid": "Milkmaid Vermeer",
    "napoleon_crossing_alps": "Napoleon Alps David",
    "raft_of_the_medusa": "Raft Medusa Gericault",
    "the_hay_wain": "Hay Wain Constable",
    "rain_steam_speed": "Rain Steam Speed Turner",
    "bar_at_folies_bergere": "Bar Folies Bergere Manet",
    "the_card_players": "Card Players Cezanne",
    "the_old_guitarist": "Old Guitarist Picasso",
    "the_dream_rousseau": "Dream Rousseau jungle",
    "tower_of_babel": "Tower Babel Bruegel",
    "christinas_world": "Christina World Wyeth",
    "the_two_fridas": "Two Fridas Kahlo",
    "broadway_boogie_woogie": "Broadway Boogie Woogie Mondrian",
    "composition_viii": "Composition Kandinsky",
    "dogs_playing_poker": "Dogs Playing Poker Coolidge",
    "lady_of_shalott": "Lady Shalott Waterhouse",
    "girl_before_mirror": "Girl Mirror Picasso",
    "luncheon_boating_party": "Luncheon Boating Party Renoir",
    "birth_of_the_world": "Birth World Miro",
    "a_bigger_splash": "Bigger Splash Hockney",
    "campbell_soup_cans": "Campbell Soup Warhol",
    "number_five_1948": "Number Five Pollock",
    "the_blue_boy": "Blue Boy Gainsborough",
    "treachery_of_images": "Treachery Images Magritte pipe",
    "luncheon_on_the_grass": "Dejeuner herbe Manet",
}

total = len(PAINTINGS)
done = 0
failed = []

for i, (pid, tw, th, urls) in enumerate(PAINTINGS):
    painting_path = os.path.join(PAINTING_DIR, f"{pid}.png")
    item_path = os.path.join(ITEM_DIR, f"{pid}.png")
    if os.path.exists(painting_path) and os.path.exists(item_path):
        print(f"[{i+1}/{total}] SKIP {pid}")
        done += 1
        continue

    success = False

    # Try direct URLs first
    for url in urls:
        try:
            print(f"[{i+1}/{total}] Trying direct URL for {pid}...", end=" ", flush=True)
            data = try_download(url)
            img = Image.open(io.BytesIO(data)).convert("RGB")
            painting = img.resize((tw, th), Image.LANCZOS)
            painting = add_frame(painting)
            painting.save(painting_path, "PNG")
            item = img.resize((32, 32), Image.LANCZOS).convert("RGBA")
            item = add_frame(item)
            item.save(item_path, "PNG")
            print(f"OK ({tw}x{th})")
            done += 1
            success = True
            break
        except Exception as e:
            print(f"FAIL: {e}")
            time.sleep(3)

    # Try Met Museum API as fallback
    if not success and pid in MET_SEARCH:
        try:
            print(f"  Trying Met Museum for {pid}...", end=" ", flush=True)
            met_url = search_met_museum(MET_SEARCH[pid])
            if met_url:
                time.sleep(2)
                data = try_download(met_url)
                img = Image.open(io.BytesIO(data)).convert("RGB")
                painting = img.resize((tw, th), Image.LANCZOS)
                painting = add_frame(painting)
                painting.save(painting_path, "PNG")
                item = img.resize((32, 32), Image.LANCZOS).convert("RGBA")
                item = add_frame(item)
                item.save(item_path, "PNG")
                print(f"OK ({tw}x{th})")
                done += 1
                success = True
            else:
                print("not found in Met collection")
        except Exception as e:
            print(f"FAIL: {e}")

    if not success:
        failed.append(pid)

    time.sleep(8)

print(f"\nComplete: {done}/{total} succeeded")
if failed:
    print(f"Failed ({len(failed)}): {', '.join(failed)}")
