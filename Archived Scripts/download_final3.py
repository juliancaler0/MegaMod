"""Fix protocol-relative URLs and retry."""
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

def try_url(url):
    if url.startswith("//"):
        url = "https:" + url
    req = urllib.request.Request(url, headers={
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    })
    with urllib.request.urlopen(req, timeout=20, context=ctx) as resp:
        return resp.read()

def get_wiki_image_url(filename, wikis=None):
    if wikis is None:
        wikis = ["en", "commons"]
    for src in wikis:
        try:
            if src == "commons":
                url = f"https://api.wikimedia.org/core/v1/commons/file/File:{urllib.request.quote(filename, safe='')}"
            else:
                url = f"https://api.wikimedia.org/core/v1/wikipedia/{src}/file/File:{urllib.request.quote(filename, safe='')}"
            req = urllib.request.Request(url, headers={
                "User-Agent": "MegaModMuseum/1.0 (julian@example.com)",
            })
            with urllib.request.urlopen(req, timeout=15, context=ctx) as resp:
                data = json.loads(resp.read().decode())
            for key in ["thumbnail", "preferred", "original"]:
                entry = data.get(key, {})
                img_url = entry.get("url")
                if img_url:
                    if img_url.startswith("//"):
                        img_url = "https:" + img_url
                    return img_url
        except Exception as e:
            print(f"    ({src}: {e})")
    return None

PAINTINGS = [
    ("the_old_guitarist", 64, 128, "Old_guitarist_chicago.jpg"),
    ("treachery_of_images", 128, 64, "MagrittePipe.jpg"),
    ("birth_of_the_world", 128, 128, "Joan Miró, 1925, The Birth of the World.jpg"),
    ("campbell_soup_cans", 64, 128, "Campbell's Soup Can (Tomato) (1962).jpg"),
    ("girl_with_balloon", 64, 128, "Albert Joseph Pénot - The Bat-Woman.jpg"),
]

done = 0
failed = []

for i, (pid, tw, th, filename) in enumerate(PAINTINGS):
    print(f"[{i+1}/{len(PAINTINGS)}] {pid}...", flush=True)
    try:
        img_url = get_wiki_image_url(filename)
        if not img_url:
            print("  No URL found in any wiki")
            failed.append(pid)
            time.sleep(8)
            continue
        print(f"  Downloading from {img_url[:60]}...", end=" ", flush=True)
        time.sleep(3)
        data = try_url(img_url)
        img = Image.open(io.BytesIO(data)).convert("RGB")
        save_both(pid, img, tw, th)
        print(f"OK ({tw}x{th})")
        done += 1
    except Exception as e:
        print(f"FAIL: {e}")
        failed.append(pid)
    time.sleep(10)

print(f"\nComplete: {done}/{len(PAINTINGS)}")
if failed:
    print(f"Failed: {', '.join(failed)}")
