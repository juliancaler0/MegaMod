"""Find correct filenames via Wikipedia search, then download."""
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

def search_commons(query):
    """Search Wikimedia Commons for an image and return the thumbnail URL."""
    url = f"https://commons.wikimedia.org/w/api.php?action=query&list=search&srsearch={urllib.request.quote(query)}&srnamespace=6&format=json"
    req = urllib.request.Request(url, headers={"User-Agent": "MegaModMuseum/1.0 (julian@example.com)"})
    with urllib.request.urlopen(req, timeout=15, context=ctx) as resp:
        data = json.loads(resp.read().decode())
    results = data.get("query", {}).get("search", [])
    for r in results[:3]:
        title = r.get("title", "")
        if title.startswith("File:"):
            filename = title[5:]
            # Get image info
            info_url = f"https://commons.wikimedia.org/w/api.php?action=query&titles={urllib.request.quote(title)}&prop=imageinfo&iiprop=url&iiurlwidth=640&format=json"
            req2 = urllib.request.Request(info_url, headers={"User-Agent": "MegaModMuseum/1.0 (julian@example.com)"})
            with urllib.request.urlopen(req2, timeout=15, context=ctx) as resp2:
                info = json.loads(resp2.read().decode())
            pages = info.get("query", {}).get("pages", {})
            for page in pages.values():
                ii = page.get("imageinfo", [{}])
                if ii:
                    return ii[0].get("thumburl") or ii[0].get("url")
    return None

def try_url(url):
    if url.startswith("//"):
        url = "https:" + url
    req = urllib.request.Request(url, headers={
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    })
    with urllib.request.urlopen(req, timeout=20, context=ctx) as resp:
        return resp.read()

PAINTINGS = [
    ("birth_of_the_world", 128, 128, "Miro Birth of the World painting"),
    ("campbell_soup_cans", 64, 128, "Andy Warhol Campbell Soup Can Tomato"),
    ("girl_with_balloon", 64, 128, "Albert Joseph Penot Bat-Woman painting"),
]

done = 0
failed = []

for i, (pid, tw, th, query) in enumerate(PAINTINGS):
    print(f"[{i+1}/{len(PAINTINGS)}] Searching for {pid}...", flush=True)
    try:
        img_url = search_commons(query)
        if not img_url:
            print("  No results")
            failed.append(pid)
            time.sleep(10)
            continue
        print(f"  Found: {img_url[:70]}...", end=" ", flush=True)
        time.sleep(5)
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
    print(f"Still failed: {', '.join(failed)}")
