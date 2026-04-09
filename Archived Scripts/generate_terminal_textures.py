"""
Generate textures for the Trading Terminal block.
Three 16x16 RGBA PNGs: front (with screen), side, top.
"""
from PIL import Image, ImageDraw

OUTPUT = "src/main/resources/assets/megamod/textures/block"

# Color palette - dark tech/terminal aesthetic
FRAME_DARK = (40, 40, 50, 255)
FRAME_MID = (55, 55, 65, 255)
FRAME_LIGHT = (70, 70, 80, 255)
SCREEN_BG = (20, 35, 50, 255)
SCREEN_GLOW = (40, 120, 180, 255)
SCREEN_BRIGHT = (80, 180, 220, 255)
SCREEN_TEXT = (100, 220, 255, 255)
LED_GREEN = (50, 200, 80, 255)
LED_RED = (200, 60, 60, 255)
ACCENT_GOLD = (180, 150, 60, 255)
BASE_DARK = (30, 30, 35, 255)

def save(img, name):
    img.save(f"{OUTPUT}/{name}.png")
    print(f"  Created {name}.png")

# === FRONT (screen face) ===
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
# Frame border
for x in range(16):
    for y in range(16):
        img.putpixel((x, y), FRAME_DARK)
# Inner frame
for x in range(1, 15):
    for y in range(1, 15):
        img.putpixel((x, y), FRAME_MID)
# Screen area (4,2 to 12,10)
for x in range(3, 13):
    for y in range(2, 10):
        img.putpixel((x, y), SCREEN_BG)
# Screen glow border
for x in range(3, 13):
    img.putpixel((x, 2), SCREEN_GLOW)
    img.putpixel((x, 9), SCREEN_GLOW)
for y in range(2, 10):
    img.putpixel((3, y), SCREEN_GLOW)
    img.putpixel((12, y), SCREEN_GLOW)
# Screen content lines (fake text/chart)
for x in range(5, 11):
    img.putpixel((x, 4), SCREEN_TEXT)
for x in range(5, 9):
    img.putpixel((x, 6), SCREEN_BRIGHT)
for x in range(6, 11):
    img.putpixel((x, 8), SCREEN_TEXT)
# Coin icon on screen
img.putpixel((5, 5), ACCENT_GOLD)
img.putpixel((6, 5), ACCENT_GOLD)
# LEDs below screen
img.putpixel((5, 11), LED_GREEN)
img.putpixel((7, 11), LED_GREEN)
img.putpixel((10, 11), LED_RED)
# Button area
for x in range(4, 12):
    img.putpixel((x, 13), FRAME_LIGHT)
# Gold accent stripe at bottom
for x in range(3, 13):
    img.putpixel((x, 14), ACCENT_GOLD)
save(img, "trading_terminal_front")

# === SIDE ===
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
for x in range(16):
    for y in range(16):
        img.putpixel((x, y), FRAME_DARK)
for x in range(1, 15):
    for y in range(1, 15):
        img.putpixel((x, y), FRAME_MID)
# Vent slots
for y in range(3, 12, 2):
    for x in range(3, 13):
        img.putpixel((x, y), FRAME_DARK)
# Panel line
for y in range(2, 14):
    img.putpixel((7, y), FRAME_LIGHT)
    img.putpixel((8, y), FRAME_LIGHT)
# Gold accent stripe bottom
for x in range(3, 13):
    img.putpixel((x, 14), ACCENT_GOLD)
# Base
for x in range(16):
    img.putpixel((x, 15), BASE_DARK)
save(img, "trading_terminal_side")

# === TOP ===
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
for x in range(16):
    for y in range(16):
        img.putpixel((x, y), FRAME_MID)
# Border
for x in range(16):
    img.putpixel((x, 0), FRAME_DARK)
    img.putpixel((x, 15), FRAME_DARK)
for y in range(16):
    img.putpixel((0, y), FRAME_DARK)
    img.putpixel((15, y), FRAME_DARK)
# Inner detail
for x in range(4, 12):
    for y in range(4, 12):
        img.putpixel((x, y), FRAME_LIGHT)
# Center circle/heat vent
for dx in range(-2, 3):
    for dy in range(-2, 3):
        if dx*dx + dy*dy <= 4:
            img.putpixel((8 + dx, 8 + dy), FRAME_DARK)
# Corner screws
for cx, cy in [(2,2), (13,2), (2,13), (13,13)]:
    img.putpixel((cx, cy), ACCENT_GOLD)
save(img, "trading_terminal_top")

print("\nTrading terminal textures generated!")
