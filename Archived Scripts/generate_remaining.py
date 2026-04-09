"""
Generate the 7 remaining painting textures programmatically.
These are recognizable color compositions of famous works.
"""
import os, random
from PIL import Image, ImageDraw

BASE = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures"
PAINTING_DIR = os.path.join(BASE, "painting")
ITEM_DIR = os.path.join(BASE, "item")

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

def save_painting(pid, img, tw, th):
    painting = img.resize((tw, th), Image.LANCZOS)
    painting = add_frame(painting)
    painting.save(os.path.join(PAINTING_DIR, f"{pid}.png"), "PNG")
    item = img.resize((32, 32), Image.LANCZOS).convert("RGBA")
    item = add_frame(item)
    item.save(os.path.join(ITEM_DIR, f"{pid}.png"), "PNG")
    print(f"  OK {pid} ({tw}x{th})")

# 1. The Old Guitarist - Picasso Blue Period
# Dominant blue tones, hunched figure with brown guitar
def old_guitarist():
    img = Image.new("RGB", (64, 128))
    d = ImageDraw.Draw(img)
    # Blue background gradient
    for y in range(128):
        b = int(80 + (y/128)*60)
        for x in range(64):
            img.putpixel((x, y), (15, 25+x//8, b+random.randint(-5,5)))
    # Hunched figure silhouette (darker blue)
    d.ellipse([18, 15, 46, 45], fill=(10, 18, 55))  # head
    d.polygon([(20, 45), (44, 45), (48, 100), (16, 100)], fill=(12, 20, 60))  # body
    # Guitar (brown/tan)
    d.ellipse([22, 55, 42, 80], fill=(120, 80, 30))
    d.ellipse([26, 60, 38, 75], fill=(90, 60, 20))
    d.rectangle([30, 45, 34, 60], fill=(100, 70, 25))  # neck
    # Hands
    d.rectangle([20, 60, 25, 70], fill=(140, 160, 180))
    d.rectangle([39, 65, 44, 75], fill=(140, 160, 180))
    return img

# 2. Broadway Boogie Woogie - Mondrian
# Grid of primary color squares on white/cream, yellow grid lines
def broadway_boogie():
    img = Image.new("RGB", (128, 128), (245, 242, 230))
    d = ImageDraw.Draw(img)
    colors = [(255,0,0), (0,0,255), (255,220,0), (245,242,230)]
    # Yellow grid lines
    for pos in [16, 32, 48, 64, 80, 96, 112]:
        d.rectangle([pos-1, 0, pos+1, 127], fill=(255, 220, 0))
        d.rectangle([0, pos-1, 127, pos+1], fill=(255, 220, 0))
    # Colored squares at intersections
    random.seed(42)
    for gx in [16, 32, 48, 64, 80, 96, 112]:
        for gy in [16, 32, 48, 64, 80, 96, 112]:
            if random.random() < 0.4:
                c = random.choice([(255,0,0), (0,0,255), (255,220,0)])
                sz = random.choice([3, 4, 5])
                d.rectangle([gx-sz, gy-sz, gx+sz, gy+sz], fill=c)
    # Larger blocks in some cells
    for _ in range(8):
        bx = random.choice([8, 24, 40, 56, 72, 88, 104])
        by = random.choice([8, 24, 40, 56, 72, 88, 104])
        c = random.choice([(255,0,0), (0,0,255)])
        d.rectangle([bx-4, by-4, bx+4, by+4], fill=c)
    return img

# 3. Composition VIII - Kandinsky
# Geometric shapes: circles, triangles, lines on light background
def composition_viii():
    img = Image.new("RGB", (256, 128), (235, 230, 215))
    d = ImageDraw.Draw(img)
    random.seed(8)
    # Large circle top-left (dark)
    d.ellipse([20, 10, 90, 80], fill=(30, 30, 50))
    # Overlapping circle
    d.ellipse([50, 25, 100, 75], outline=(180, 50, 50), width=2)
    # Triangles
    d.polygon([(120, 20), (160, 80), (80, 80)], outline=(40, 40, 120), width=2)
    d.polygon([(180, 30), (220, 90), (140, 90)], fill=(220, 180, 60))
    # Grid of lines
    for i in range(5):
        x1 = 130 + i*15
        d.line([(x1, 10), (x1+30, 110)], fill=(60, 60, 80), width=1)
    # Small circles scattered
    for _ in range(15):
        cx = random.randint(10, 245)
        cy = random.randint(10, 117)
        r = random.randint(3, 8)
        c = random.choice([(200,50,50), (50,50,180), (220,200,50), (50,150,50)])
        d.ellipse([cx-r, cy-r, cx+r, cy+r], fill=c)
    # Diagonal lines
    d.line([(10, 100), (246, 30)], fill=(100, 40, 40), width=2)
    d.line([(30, 5), (200, 120)], fill=(40, 40, 100), width=1)
    # Checkered pattern area
    for bx in range(200, 240, 6):
        for by in range(60, 100, 6):
            if (bx + by) % 12 < 6:
                d.rectangle([bx, by, bx+5, by+5], fill=(80, 60, 120))
    return img

# 4. Dogs Playing Poker - Coolidge
# Warm scene: green table, brown/tan dogs around it, warm lighting
def dogs_playing_poker():
    img = Image.new("RGB", (256, 128))
    d = ImageDraw.Draw(img)
    # Dark warm background (smoking room)
    for y in range(128):
        for x in range(256):
            img.putpixel((x, y), (50+y//8, 35+y//10, 20+y//12))
    # Green poker table (center)
    d.ellipse([50, 50, 206, 120], fill=(30, 90, 40))
    d.ellipse([55, 55, 201, 115], fill=(35, 110, 50))
    # Lamp overhead
    d.polygon([(110, 0), (146, 0), (160, 20), (96, 20)], fill=(180, 160, 80))
    d.rectangle([120, 20, 136, 35], fill=(200, 180, 100))
    # Dog silhouettes around table (brown/tan lumps)
    positions = [(40, 40), (85, 30), (140, 28), (195, 35), (220, 45)]
    for px, py in positions:
        # Head
        d.ellipse([px-8, py-8, px+8, py+8], fill=(160, 120, 80))
        # Body
        d.ellipse([px-12, py+5, px+12, py+35], fill=(140, 100, 60))
        # Ears
        d.ellipse([px-10, py-12, px-4, py-2], fill=(130, 90, 50))
        d.ellipse([px+4, py-12, px+10, py-2], fill=(130, 90, 50))
    # Cards on table
    for cx, cy in [(100, 75), (120, 80), (140, 78), (160, 82)]:
        d.rectangle([cx-4, cy-6, cx+4, cy+6], fill=(240, 235, 220))
        d.rectangle([cx-3, cy-5, cx-1, cy-3], fill=(200, 30, 30))
    # Chips
    for cx, cy in [(110, 90), (145, 85), (130, 92)]:
        d.ellipse([cx-3, cy-1, cx+3, cy+1], fill=(200, 50, 50))
        d.ellipse([cx-3, cy-3, cx+3, cy-1], fill=(50, 50, 180))
    return img

# 5. Birth of the World - Miró
# Abstract: earth-tone background, black biomorphic shapes, red/yellow accents
def birth_of_world():
    img = Image.new("RGB", (128, 128))
    d = ImageDraw.Draw(img)
    # Washy background gradient (earth tones fading to grey)
    for y in range(128):
        for x in range(128):
            r = int(180 - y*0.5 + random.randint(-10, 10))
            g = int(170 - y*0.6 + random.randint(-10, 10))
            b = int(150 - y*0.3 + random.randint(-10, 10))
            img.putpixel((x, y), (max(0,min(255,r)), max(0,min(255,g)), max(0,min(255,b))))
    # Black triangle (main shape)
    d.polygon([(30, 20), (70, 100), (10, 90)], fill=(10, 10, 15))
    # Red circle
    d.ellipse([80, 15, 105, 40], fill=(200, 30, 30))
    # Yellow star/dot
    d.ellipse([50, 5, 60, 15], fill=(240, 210, 40))
    # Black line
    d.line([(90, 40), (100, 110)], fill=(10, 10, 10), width=2)
    # Small blue shape
    d.ellipse([95, 80, 115, 100], fill=(30, 50, 150))
    # Thin black curved line (approximated)
    for t in range(50):
        x = int(20 + t*2 + 10*((t%10)/10))
        y = int(60 + 15*(t%15)/15)
        if 0 <= x < 128 and 0 <= y < 128:
            d.point((x, y), fill=(10, 10, 10))
    return img

# 6. Campbell's Soup Cans - Warhol
# Red and white soup can on light background
def campbell_soup():
    img = Image.new("RGB", (64, 128), (245, 240, 230))
    d = ImageDraw.Draw(img)
    # Can body
    can_left, can_right = 12, 52
    can_top, can_bot = 15, 110
    # White top half
    d.rectangle([can_left, can_top, can_right, 60], fill=(240, 235, 225))
    # Red bottom half
    d.rectangle([can_left, 60, can_right, can_bot], fill=(190, 30, 30))
    # Gold band at middle
    d.rectangle([can_left, 55, can_right, 65], fill=(200, 170, 50))
    # Gold rim top
    d.rectangle([can_left-2, can_top-3, can_right+2, can_top+2], fill=(180, 160, 80))
    # Gold rim bottom
    d.rectangle([can_left-2, can_bot-2, can_right+2, can_bot+3], fill=(180, 160, 80))
    # Fleur-de-lis / medallion (gold circle in center)
    d.ellipse([26, 57, 38, 63], fill=(210, 180, 60))
    # "SOUP" text area (dark red on red)
    d.rectangle([16, 75, 48, 90], fill=(170, 20, 20))
    # Lighter text hint
    for x in range(20, 45, 3):
        d.rectangle([x, 78, x+1, 87], fill=(220, 180, 160))
    # Campbell's text hint on white area
    for x in range(18, 46, 2):
        d.rectangle([x, 30, x+1, 38], fill=(180, 30, 30))
    return img

# 7. Treachery of Images - Magritte (This is not a pipe)
# Beige/cream background, brown pipe, text below
def treachery_of_images():
    img = Image.new("RGB", (128, 64), (225, 215, 190))
    d = ImageDraw.Draw(img)
    # Pipe bowl (dark brown)
    d.ellipse([55, 8, 85, 30], fill=(80, 45, 20))
    d.ellipse([58, 10, 82, 28], fill=(100, 55, 25))
    # Pipe stem (extending left)
    d.rectangle([25, 22, 58, 28], fill=(70, 40, 15))
    # Pipe mouthpiece
    d.rectangle([20, 24, 28, 26], fill=(50, 30, 10))
    # Bowl opening
    d.ellipse([60, 6, 80, 14], fill=(60, 35, 15))
    d.ellipse([62, 7, 78, 13], fill=(30, 20, 10))
    # Smoke wisps
    for i in range(3):
        sx = 70 + i*3
        for sy in range(0, 8):
            if random.random() < 0.5:
                d.point((sx + random.randint(-1,1), sy), fill=(200, 195, 180))
    # "Ceci n'est pas une pipe" text (just a line of small marks)
    text_y = 42
    # Simple representation of cursive text
    for x in range(30, 98):
        if random.random() < 0.6:
            h = random.randint(0, 3)
            d.line([(x, text_y+h), (x, text_y+4+h)], fill=(40, 30, 20), width=1)
    return img

# Generate all 7
print("Generating remaining 7 paintings...")
generators = {
    "the_old_guitarist": (old_guitarist, 64, 128),
    "broadway_boogie_woogie": (broadway_boogie, 128, 128),
    "composition_viii": (composition_viii, 256, 128),
    "dogs_playing_poker": (dogs_playing_poker, 256, 128),
    "birth_of_the_world": (birth_of_world, 128, 128),
    "campbell_soup_cans": (campbell_soup, 64, 128),
    "treachery_of_images": (treachery_of_images, 128, 64),
}

for pid, (gen_func, tw, th) in generators.items():
    img = gen_func()
    save_painting(pid, img, tw, th)

print("Done! All 32 new paintings complete.")
