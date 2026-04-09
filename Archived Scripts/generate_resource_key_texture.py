"""Generate a 16x16 silver key with a blue gem at the top for the Resource Dimension Key."""
from PIL import Image
import os

OUTPUT_DIR = os.path.join("src", "main", "resources", "assets", "megamod", "textures", "item")
os.makedirs(OUTPUT_DIR, exist_ok=True)

img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
px = img.load()

# Color palette
_ = (0, 0, 0, 0)        # transparent
O = (45, 48, 58, 255)    # outline (dark)
H = (215, 220, 232, 255) # silver highlight
L = (185, 190, 202, 255) # silver light
M = (150, 155, 168, 255) # silver mid
D = (110, 115, 128, 255) # silver dark
S = (75, 78, 90, 255)    # silver shadow

# Blue gem
a = (180, 228, 255, 255) # gem highlight (sparkle)
b = (105, 190, 255, 255) # gem light
c = (55, 145, 240, 255)  # gem mid
d = (35, 100, 205, 255)  # gem dark
e = (22, 65, 160, 255)   # gem deep
g = (70, 150, 240, 70)   # gem glow (semi-transparent)

# 16x16 grid, row by row (y=0 is top)
grid = [
#    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
    [_, _, _, _, _, _, O, O, O, O, _, _, _, _, _, _],  # 0 - bow top
    [_, _, _, _, O, O, H, L, M, D, O, O, _, _, _, _],  # 1 - bow upper
    [_, _, _, O, H, L, g, a, b, g, D, S, O, _, _, _],  # 2 - bow + gem top
    [_, _, _, O, L, M, g, c, a, g, D, S, O, _, _, _],  # 3 - bow + gem mid
    [_, _, _, O, M, D, g, d, c, g, S, S, O, _, _, _],  # 4 - bow + gem low
    [_, _, _, O, D, S, _, e, d, _, S, S, O, _, _, _],  # 5 - bow + gem bottom
    [_, _, _, _, O, O, _, _, _, _, O, O, _, _, _, _],  # 6 - bow lower
    [_, _, _, _, _, _, O, L, D, O, _, _, _, _, _, _],  # 7 - bow-shaft junction
    [_, _, _, _, _, _, O, H, M, O, _, _, _, _, _, _],  # 8 - shaft (decorative ring)
    [_, _, _, _, _, _, O, L, D, O, _, _, _, _, _, _],  # 9 - shaft
    [_, _, _, _, _, _, O, L, D, O, _, _, _, _, _, _],  # 10 - shaft
    [_, _, _, _, _, _, O, L, D, O, _, _, _, _, _, _],  # 11 - shaft
    [_, _, _, _, _, _, O, L, D, M, D, O, _, _, _, _],  # 12 - shaft + tooth 1
    [_, _, _, _, _, _, O, L, D, O, O, _, _, _, _, _],  # 13 - shaft (gap)
    [_, _, _, _, _, _, O, L, D, M, D, S, O, _, _, _],  # 14 - shaft + tooth 2
    [_, _, _, _, _, _, O, O, O, O, O, O, _, _, _, _],  # 15 - bottom outline
]

for y in range(16):
    for x in range(16):
        if grid[y][x] != _:
            px[x, y] = grid[y][x]

img.save(os.path.join(OUTPUT_DIR, "resource_dimension_key.png"))
print("Generated resource_dimension_key.png (v3 - grid)")
