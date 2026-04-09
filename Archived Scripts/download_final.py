"""
Download remaining 7 paintings + replace girl_with_balloon with Bat-Woman.
Try multiple alternative sources.
"""
import os, time, urllib.request, io, json, ssl
from PIL import Image

BASE = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures"
PAINTING_DIR = os.path.join(BASE, "painting")
ITEM_DIR = os.path.join(BASE, "item")

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

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

def save_both(pid, img, tw, th):
    painting = img.resize((tw, th), Image.LANCZOS)
    painting = add_frame(painting)
    painting.save(os.path.join(PAINTING_DIR, f"{pid}.png"), "PNG")
    item = img.resize((32, 32), Image.LANCZOS).convert("RGBA")
    item = add_frame(item)
    item.save(os.path.join(ITEM_DIR, f"{pid}.png"), "PNG")

def try_url(url):
    req = urllib.request.Request(url, headers={
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept": "image/*,*/*",
    })
    with urllib.request.urlopen(req, timeout=20, context=ctx) as resp:
        return resp.read()

def try_wikimedia_rest(filename):
    """Use Wikimedia REST API (different endpoint, might not be blocked)."""
    url = f"https://api.wikimedia.org/core/v1/commons/file/File:{filename}"
    req = urllib.request.Request(url, headers={
        "User-Agent": "MegaModMuseum/1.0 (julian@example.com)",
        "Accept": "application/json"
    })
    with urllib.request.urlopen(req, timeout=15, context=ctx) as resp:
        data = json.loads(resp.read().decode())
    thumb = data.get("thumbnail", {}).get("url")
    if thumb:
        return thumb
    preferred = data.get("preferred", {}).get("url")
    return preferred

# Paintings to download: (id, target_w, target_h, [list of (source_type, value)])
PAINTINGS = [
    ("the_old_guitarist", 64, 128, [
        ("wiki_rest", "Old_guitarist_chicago.jpg"),
        ("direct", "https://www.artic.edu/iiif/2/2bed7eb2-0102-9076-a3d4-5765e397f048/full/400,/0/default.jpg"),
    ]),
    ("broadway_boogie_woogie", 128, 128, [
        ("wiki_rest", "Piet_Mondriaan%2C_1942_-_Broadway_Boogie_Woogie.jpg"),
    ]),
    ("composition_viii", 256, 128, [
        ("wiki_rest", "Vassily_Kandinsky%2C_1923_-_Composition_8%2C_huile_sur_toile%2C_140_cm_x_201_cm%2C_Mus%C3%A9e_Guggenheim%2C_New_York.jpg"),
    ]),
    ("dogs_playing_poker", 256, 128, [
        ("wiki_rest", "A_Friend_in_Need_1903_C.M.Coolidge.jpg"),
    ]),
    ("birth_of_the_world", 128, 128, [
        ("wiki_rest", "Joan_Mir%C3%B3%2C_1925%2C_The_Birth_of_the_World.jpg"),
    ]),
    ("campbell_soup_cans", 64, 128, [
        ("wiki_rest", "Campbell%27s_Soup_Can_%28Tomato%29_%281962%29.jpg"),
    ]),
    ("treachery_of_images", 128, 64, [
        ("wiki_rest", "MagrittePipe.jpg"),
    ]),
    # Replace girl_with_balloon with The Bat-Woman
    ("girl_with_balloon", 64, 128, [
        ("wiki_rest", "Albert_Joseph_P%C3%A9not_-_The_Bat-Woman.jpg"),
        ("direct", "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6a/Albert_Joseph_P%C3%A9not_-_The_Bat-Woman.jpg/320px-Albert_Joseph_P%C3%A9not_-_The_Bat-Woman.jpg"),
    ]),
]

total = len(PAINTINGS)
done = 0
failed = []

for i, (pid, tw, th, sources) in enumerate(PAINTINGS):
    print(f"[{i+1}/{total}] {pid}...", flush=True)
    success = False

    for source_type, value in sources:
        try:
            if source_type == "wiki_rest":
                print(f"  REST API: {value[:50]}...", end=" ", flush=True)
                img_url = try_wikimedia_rest(value)
                if not img_url:
                    print("no URL")
                    continue
                time.sleep(2)
                data = try_url(img_url)
            elif source_type == "direct":
                print(f"  Direct: {value[:50]}...", end=" ", flush=True)
                data = try_url(value)

            img = Image.open(io.BytesIO(data)).convert("RGB")
            save_both(pid, img, tw, th)
            print(f"OK ({tw}x{th})")
            success = True
            done += 1
            break
        except Exception as e:
            print(f"FAIL: {e}")
            time.sleep(3)

    if not success:
        failed.append(pid)

    if i < total - 1:
        time.sleep(10)

print(f"\nComplete: {done}/{total}")
if failed:
    print(f"Failed: {', '.join(failed)}")
