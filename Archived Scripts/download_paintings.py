"""
Downloads 32 real painting images from Wikimedia Commons and resizes them
for the MegaMod museum system.
- Painting textures: variable sizes based on aspect ratio (multiples of 64)
- Item textures: 32x32 with ornate frame
"""
import os
import time
import urllib.request
import io
from PIL import Image

BASE = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures"
PAINTING_DIR = os.path.join(BASE, "painting")
ITEM_DIR = os.path.join(BASE, "item")

# Wikimedia Commons thumb URLs (public domain artwork)
# Format: (id, wiki_filename, target_width, target_height)
PAINTINGS = [
    ("las_meninas", "https://upload.wikimedia.org/wikipedia/commons/thumb/3/31/Las_Meninas%2C_by_Diego_Vel%C3%A1zquez%2C_from_Prado_in_Google_Earth.jpg/400px-Las_Meninas%2C_by_Diego_Vel%C3%A1zquez%2C_from_Prado_in_Google_Earth.jpg", 128, 192),
    ("school_of_athens", "https://upload.wikimedia.org/wikipedia/commons/thumb/4/49/%22The_School_of_Athens%22_by_Raffaello_Sanzio_da_Urbino.jpg/400px-%22The_School_of_Athens%22_by_Raffaello_Sanzio_da_Urbino.jpg", 256, 128),
    ("garden_of_earthly_delights", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/96/The_Garden_of_earthly_delights.jpg/400px-The_Garden_of_earthly_delights.jpg", 256, 128),
    ("the_milkmaid", "https://upload.wikimedia.org/wikipedia/commons/thumb/2/20/Johannes_Vermeer_-_Het_melkmeisje_-_Google_Art_Project.jpg/400px-Johannes_Vermeer_-_Het_melkmeisje_-_Google_Art_Project.jpg", 128, 128),
    ("napoleon_crossing_alps", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/David_-_Napoleon_crossing_the_Alps_-_Malmaison2.jpg/400px-David_-_Napoleon_crossing_the_Alps_-_Malmaison2.jpg", 128, 192),
    ("raft_of_the_medusa", "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/JEAN_LOUIS_TH%C3%89ODORE_G%C3%89RICAULT_-_La_Balsa_de_la_Medusa_%28Museo_del_Louvre%2C_1818-19%29.jpg/400px-JEAN_LOUIS_TH%C3%89ODORE_G%C3%89RICAULT_-_La_Balsa_de_la_Medusa_%28Museo_del_Louvre%2C_1818-19%29.jpg", 256, 192),
    ("saturn_devouring_son", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/Francisco_de_Goya%2C_Saturno_devorando_a_su_hijo_%281819-1823%29.jpg/400px-Francisco_de_Goya%2C_Saturno_devorando_a_su_hijo_%281819-1823%29.jpg", 64, 128),
    ("the_hay_wain", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/John_Constable_-_The_Hay_Wain_%281821%29.jpg/400px-John_Constable_-_The_Hay_Wain_%281821%29.jpg", 256, 128),
    ("rain_steam_speed", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Turner_-_Rain%2C_Steam_and_Speed_-_National_Gallery_file.jpg/400px-Turner_-_Rain%2C_Steam_and_Speed_-_National_Gallery_file.jpg", 128, 128),
    ("ophelia", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/94/John_Everett_Millais_-_Ophelia_-_Google_Art_Project.jpg/400px-John_Everett_Millais_-_Ophelia_-_Google_Art_Project.jpg", 256, 128),
    ("bar_at_folies_bergere", "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Edouard_Manet%2C_A_Bar_at_the_Folies-Berg%C3%A8re.jpg/400px-Edouard_Manet%2C_A_Bar_at_the_Folies-Berg%C3%A8re.jpg", 256, 128),
    ("the_card_players", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Paul_C%C3%A9zanne_-_Les_Joueurs_de_cartes.jpg/400px-Paul_C%C3%A9zanne_-_Les_Joueurs_de_cartes.jpg", 128, 128),
    ("the_old_guitarist", "https://upload.wikimedia.org/wikipedia/en/thumb/b/bc/Old_guitarist_chicago.jpg/400px-Old_guitarist_chicago.jpg", 64, 128),
    ("the_dream_rousseau", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d0/Henri_Rousseau_-_Il_sogno.jpg/400px-Henri_Rousseau_-_Il_sogno.jpg", 256, 128),
    ("tower_of_babel", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fc/Pieter_Bruegel_the_Elder_-_The_Tower_of_Babel_%28Vienna%29_-_Google_Art_Project_-_edited.jpg/400px-Pieter_Bruegel_the_Elder_-_The_Tower_of_Babel_%28Vienna%29_-_Google_Art_Project_-_edited.jpg", 128, 128),
    ("christinas_world", "https://upload.wikimedia.org/wikipedia/en/thumb/a/a3/Christinasworld.jpg/400px-Christinasworld.jpg", 256, 128),
    ("the_two_fridas", "https://upload.wikimedia.org/wikipedia/en/thumb/d/d9/The_Two_Fridas.jpg/400px-The_Two_Fridas.jpg", 128, 128),
    ("broadway_boogie_woogie", "https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Piet_Mondriaan%2C_1942_-_Broadway_Boogie_Woogie.jpg/400px-Piet_Mondriaan%2C_1942_-_Broadway_Boogie_Woogie.jpg", 128, 128),
    ("composition_viii", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b4/Vassily_Kandinsky%2C_1923_-_Composition_8%2C_huile_sur_toile%2C_140_cm_x_201_cm%2C_Mus%C3%A9e_Guggenheim%2C_New_York.jpg/400px-Vassily_Kandinsky%2C_1923_-_Composition_8%2C_huile_sur_toile%2C_140_cm_x_201_cm%2C_Mus%C3%A9e_Guggenheim%2C_New_York.jpg", 256, 128),
    ("dogs_playing_poker", "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6e/A_Friend_in_Need_1903_C.M.Coolidge.jpg/400px-A_Friend_in_Need_1903_C.M.Coolidge.jpg", 256, 128),
    ("third_of_may", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/El_Tres_de_Mayo%2C_by_Francisco_de_Goya%2C_from_Prado_thin_black_margin.jpg/400px-El_Tres_de_Mayo%2C_by_Francisco_de_Goya%2C_from_Prado_thin_black_margin.jpg", 256, 128),
    ("lady_of_shalott", "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/John_William_Waterhouse_-_The_Lady_of_Shalott_-_Google_Art_Project.jpg/400px-John_William_Waterhouse_-_The_Lady_of_Shalott_-_Google_Art_Project.jpg", 128, 128),
    ("girl_before_mirror", "https://upload.wikimedia.org/wikipedia/en/thumb/0/0e/Picasso_Girl_before_a_Mirror_1932.jpg/400px-Picasso_Girl_before_a_Mirror_1932.jpg", 128, 128),
    ("luncheon_boating_party", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8d/Pierre-Auguste_Renoir_-_Luncheon_of_the_Boating_Party_-_Google_Art_Project.jpg/400px-Pierre-Auguste_Renoir_-_Luncheon_of_the_Boating_Party_-_Google_Art_Project.jpg", 256, 192),
    ("birth_of_the_world", "https://upload.wikimedia.org/wikipedia/en/thumb/2/25/Joan_Mir%C3%B3%2C_1925%2C_The_Birth_of_the_World.jpg/400px-Joan_Mir%C3%B3%2C_1925%2C_The_Birth_of_the_World.jpg", 128, 128),
    ("a_bigger_splash", "https://upload.wikimedia.org/wikipedia/en/thumb/5/55/A_Bigger_Splash.jpg/400px-A_Bigger_Splash.jpg", 128, 128),
    ("campbell_soup_cans", "https://upload.wikimedia.org/wikipedia/en/thumb/9/95/Campbell%27s_Soup_Can_%28Tomato%29_%281962%29.jpg/400px-Campbell%27s_Soup_Can_%28Tomato%29_%281962%29.jpg", 64, 128),
    ("number_five_1948", "https://upload.wikimedia.org/wikipedia/en/thumb/4/4a/No._5%2C_1948.jpg/400px-No._5%2C_1948.jpg", 128, 128),
    ("anatomy_lesson", "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4d/Rembrandt_-_The_Anatomy_Lesson_of_Dr_Nicolaes_Tulp.jpg/400px-Rembrandt_-_The_Anatomy_Lesson_of_Dr_Nicolaes_Tulp.jpg", 256, 128),
    ("the_blue_boy", "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/Thomas_Gainsborough_-_The_Blue_Boy_%28The_Huntington_Library%2C_San_Marino_L._A.%29.jpg/400px-Thomas_Gainsborough_-_The_Blue_Boy_%28The_Huntington_Library%2C_San_Marino_L._A.%29.jpg", 64, 128),
    ("treachery_of_images", "https://upload.wikimedia.org/wikipedia/en/thumb/b/b9/MasrittePipe.jpg/400px-MagrittePipe.jpg", 128, 64),
    ("luncheon_on_the_grass", "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/%C3%89douard_Manet_-_Le_D%C3%A9jeuner_sur_l%27herbe.jpg/400px-%C3%89douard_Manet_-_Le_D%C3%A9jeuner_sur_l%27herbe.jpg", 256, 128),
]

def add_frame(img):
    """Add 2px ornate frame matching existing paintings."""
    w, h = img.size
    framed = img.copy()
    dark_wood = (42, 26, 14)
    mid_wood = (90, 58, 30)
    gold = (139, 105, 20)
    # Outer pixel border
    for x in range(w):
        framed.putpixel((x, 0), dark_wood)
        framed.putpixel((x, h-1), dark_wood)
    for y in range(h):
        framed.putpixel((0, y), dark_wood)
        framed.putpixel((w-1, y), dark_wood)
    # Inner pixel border
    for x in range(1, w-1):
        framed.putpixel((x, 1), mid_wood)
        framed.putpixel((x, h-2), mid_wood)
    for y in range(1, h-1):
        framed.putpixel((1, y), mid_wood)
        framed.putpixel((w-1-1, y), mid_wood)
    # Gold corner accents
    for cx, cy in [(0,0), (w-1,0), (0,h-1), (w-1,h-1)]:
        framed.putpixel((cx, cy), gold)
        if cx+1 < w: framed.putpixel((cx+1, cy), gold)
        if cx-1 >= 0: framed.putpixel((cx-1, cy), gold)
        if cy+1 < h: framed.putpixel((cx, cy+1), gold)
        if cy-1 >= 0: framed.putpixel((cx, cy-1), gold)
    return framed

def download_and_process(painting_id, url, target_w, target_h, batch_num):
    painting_path = os.path.join(PAINTING_DIR, f"{painting_id}.png")
    item_path = os.path.join(ITEM_DIR, f"{painting_id}.png")

    if os.path.exists(painting_path) and os.path.exists(item_path):
        print(f"  [SKIP] {painting_id} - already exists")
        return True

    try:
        req = urllib.request.Request(url, headers={"User-Agent": "MegaModBot/1.0 (Minecraft mod texture download)"})
        with urllib.request.urlopen(req, timeout=15) as response:
            data = response.read()

        img = Image.open(io.BytesIO(data)).convert("RGB")

        # Painting texture (variable size with frame)
        painting = img.resize((target_w, target_h), Image.LANCZOS)
        painting = add_frame(painting)
        painting.save(painting_path, "PNG")

        # Item texture (32x32 RGBA with frame)
        item = img.resize((32, 32), Image.LANCZOS).convert("RGBA")
        item = add_frame(item)
        item.save(item_path, "PNG")

        print(f"  [OK] {painting_id} ({target_w}x{target_h})")
        return True
    except Exception as e:
        print(f"  [FAIL] {painting_id}: {e}")
        return False

# Process in batches of 8 with delays
batch_size = 8
for i in range(0, len(PAINTINGS), batch_size):
    batch = PAINTINGS[i:i+batch_size]
    batch_num = i // batch_size + 1
    print(f"\nBatch {batch_num} ({len(batch)} paintings):")
    for pid, url, tw, th in batch:
        download_and_process(pid, url, tw, th, batch_num)
        time.sleep(1)  # 1 second between downloads
    if i + batch_size < len(PAINTINGS):
        print(f"  Waiting 5s before next batch...")
        time.sleep(5)

print("\nDone!")
