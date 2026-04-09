"""Retry failed painting downloads with longer delays."""
import os, time, urllib.request, io
from PIL import Image

BASE = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures"
PAINTING_DIR = os.path.join(BASE, "painting")
ITEM_DIR = os.path.join(BASE, "item")

# Only paintings that failed - with corrected/alternative URLs
PAINTINGS = [
    ("garden_of_earthly_delights", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/96/The_Garden_of_earthly_delights.jpg/300px-The_Garden_of_earthly_delights.jpg", 256, 128),
    ("the_milkmaid", "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/Johannes_Vermeer_-_Het_melkmeisje_-_Google_Art_Project.jpg/300px-Johannes_Vermeer_-_Het_melkmeisje_-_Google_Art_Project.jpg", 128, 128),
    ("napoleon_crossing_alps", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/David_-_Napoleon_crossing_the_Alps_-_Malmaison2.jpg/300px-David_-_Napoleon_crossing_the_Alps_-_Malmaison2.jpg", 128, 192),
    ("raft_of_the_medusa", "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/JEAN_LOUIS_TH%C3%89ODORE_G%C3%89RICAULT_-_La_Balsa_de_la_Medusa_%28Museo_del_Louvre%2C_1818-19%29.jpg/300px-JEAN_LOUIS_TH%C3%89ODORE_G%C3%89RICAULT_-_La_Balsa_de_la_Medusa_%28Museo_del_Louvre%2C_1818-19%29.jpg", 256, 192),
    ("the_hay_wain", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/John_Constable_-_The_Hay_Wain_%281821%29.jpg/300px-John_Constable_-_The_Hay_Wain_%281821%29.jpg", 256, 128),
    ("rain_steam_speed", "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7e/Rain_Steam_and_Speed_the_Great_Western_Railway.jpg/300px-Rain_Steam_and_Speed_the_Great_Western_Railway.jpg", 128, 128),
    ("bar_at_folies_bergere", "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Edouard_Manet%2C_A_Bar_at_the_Folies-Berg%C3%A8re.jpg/300px-Edouard_Manet%2C_A_Bar_at_the_Folies-Berg%C3%A8re.jpg", 256, 128),
    ("the_card_players", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Paul_C%C3%A9zanne_-_Les_Joueurs_de_cartes.jpg/300px-Paul_C%C3%A9zanne_-_Les_Joueurs_de_cartes.jpg", 128, 128),
    ("the_old_guitarist", "https://upload.wikimedia.org/wikipedia/en/thumb/b/bc/Old_guitarist_chicago.jpg/300px-Old_guitarist_chicago.jpg", 64, 128),
    ("the_dream_rousseau", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/Henri_Rousseau_-_Il_sogno.jpg/300px-Henri_Rousseau_-_Il_sogno.jpg", 256, 128),
    ("tower_of_babel", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fc/Pieter_Bruegel_the_Elder_-_The_Tower_of_Babel_%28Vienna%29_-_Google_Art_Project_-_edited.jpg/300px-Pieter_Bruegel_the_Elder_-_The_Tower_of_Babel_%28Vienna%29_-_Google_Art_Project_-_edited.jpg", 128, 128),
    ("christinas_world", "https://upload.wikimedia.org/wikipedia/en/thumb/a/a3/Christinasworld.jpg/300px-Christinasworld.jpg", 256, 128),
    ("the_two_fridas", "https://upload.wikimedia.org/wikipedia/en/thumb/d/d9/The_Two_Fridas.jpg/300px-The_Two_Fridas.jpg", 128, 128),
    ("broadway_boogie_woogie", "https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Piet_Mondriaan%2C_1942_-_Broadway_Boogie_Woogie.jpg/300px-Piet_Mondriaan%2C_1942_-_Broadway_Boogie_Woogie.jpg", 128, 128),
    ("composition_viii", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/Vassily_Kandinsky%2C_1923_-_Composition_8%2C_huile_sur_toile%2C_140_cm_x_201_cm%2C_Mus%C3%A9e_Guggenheim%2C_New_York.jpg/300px-Vassily_Kandinsky%2C_1923_-_Composition_8%2C_huile_sur_toile%2C_140_cm_x_201_cm%2C_Mus%C3%A9e_Guggenheim%2C_New_York.jpg", 256, 128),
    ("dogs_playing_poker", "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6e/A_Friend_in_Need_1903_C.M.Coolidge.jpg/300px-A_Friend_in_Need_1903_C.M.Coolidge.jpg", 256, 128),
    ("lady_of_shalott", "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/John_William_Waterhouse_-_The_Lady_of_Shalott_-_Google_Art_Project.jpg/300px-John_William_Waterhouse_-_The_Lady_of_Shalott_-_Google_Art_Project.jpg", 128, 128),
    ("girl_before_mirror", "https://upload.wikimedia.org/wikipedia/en/thumb/0/0e/Picasso_Girl_before_a_Mirror_1932.jpg/300px-Picasso_Girl_before_a_Mirror_1932.jpg", 128, 128),
    ("luncheon_boating_party", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8d/Pierre-Auguste_Renoir_-_Luncheon_of_the_Boating_Party_-_Google_Art_Project.jpg/300px-Pierre-Auguste_Renoir_-_Luncheon_of_the_Boating_Party_-_Google_Art_Project.jpg", 256, 192),
    ("birth_of_the_world", "https://upload.wikimedia.org/wikipedia/en/thumb/2/25/Joan_Mir%C3%B3%2C_1925%2C_The_Birth_of_the_World.jpg/300px-Joan_Mir%C3%B3%2C_1925%2C_The_Birth_of_the_World.jpg", 128, 128),
    ("a_bigger_splash", "https://upload.wikimedia.org/wikipedia/en/thumb/5/55/A_Bigger_Splash.jpg/300px-A_Bigger_Splash.jpg", 128, 128),
    ("campbell_soup_cans", "https://upload.wikimedia.org/wikipedia/en/thumb/9/95/Campbell%27s_Soup_Can_%28Tomato%29_%281962%29.jpg/300px-Campbell%27s_Soup_Can_%28Tomato%29_%281962%29.jpg", 64, 128),
    ("number_five_1948", "https://upload.wikimedia.org/wikipedia/en/thumb/4/4a/No._5%2C_1948.jpg/300px-No._5%2C_1948.jpg", 128, 128),
    ("the_blue_boy", "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/Thomas_Gainsborough_-_The_Blue_Boy_%28The_Huntington_Library%2C_San_Marino_L._A.%29.jpg/300px-Thomas_Gainsborough_-_The_Blue_Boy_%28The_Huntington_Library%2C_San_Marino_L._A.%29.jpg", 64, 128),
    ("treachery_of_images", "https://upload.wikimedia.org/wikipedia/en/thumb/b/b9/MagrittePipe.jpg/300px-MagrittePipe.jpg", 128, 64),
    ("luncheon_on_the_grass", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/%C3%89douard_Manet_-_Le_D%C3%A9jeuner_sur_l%27herbe.jpg/300px-%C3%89douard_Manet_-_Le_D%C3%A9jeuner_sur_l%27herbe.jpg", 256, 128),
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

total = len(PAINTINGS)
done = 0
failed = []

for i, (pid, url, tw, th) in enumerate(PAINTINGS):
    painting_path = os.path.join(PAINTING_DIR, f"{pid}.png")
    item_path = os.path.join(ITEM_DIR, f"{pid}.png")

    if os.path.exists(painting_path) and os.path.exists(item_path):
        print(f"[{i+1}/{total}] SKIP {pid}")
        done += 1
        continue

    try:
        req = urllib.request.Request(url, headers={
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        })
        with urllib.request.urlopen(req, timeout=20) as response:
            data = response.read()
        img = Image.open(io.BytesIO(data)).convert("RGB")
        painting = img.resize((tw, th), Image.LANCZOS)
        painting = add_frame(painting)
        painting.save(painting_path, "PNG")
        item = img.resize((32, 32), Image.LANCZOS).convert("RGBA")
        item = add_frame(item)
        item.save(item_path, "PNG")
        print(f"[{i+1}/{total}] OK {pid} ({tw}x{th})")
        done += 1
    except Exception as e:
        print(f"[{i+1}/{total}] FAIL {pid}: {e}")
        failed.append(pid)

    # 15 second delay between each download
    if i < total - 1:
        time.sleep(15)

print(f"\nComplete: {done}/{total} succeeded")
if failed:
    print(f"Failed: {', '.join(failed)}")
