from PIL import Image
import math, random

OUT = 'src/main/resources/assets/megamod/textures/item/'
random.seed(42)

def lerp(a, b, t):
    n = min(len(a), len(b), 3)  # always lerp RGB only
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(n))

# 1. STRANGE MEAT
img = Image.new('RGBA', (32, 32), (0,0,0,0))
p = img.load()
bone_light = (220, 210, 190, 255)
bone_mid = (195, 185, 165, 255)
bone_dark = (160, 150, 130, 255)
bone_outline = (100, 85, 70, 255)
for i in range(14):
    bx, by = 4 + i, 27 - i
    for dx in range(-1, 2):
        for dy in range(-1, 2):
            x, y = bx+dx, by+dy
            if 0 <= x < 32 and 0 <= y < 32:
                if abs(dx)+abs(dy) == 2: p[x,y] = bone_outline
                elif dx == -1 or dy == 1: p[x,y] = bone_dark
                elif dx == 1 or dy == -1: p[x,y] = bone_light
                else: p[x,y] = bone_mid
for dx in range(-2, 3):
    for dy in range(-2, 3):
        dist = abs(dx) + abs(dy)
        if dist <= 3:
            x, y = 3+dx, 28+dy
            if 0 <= x < 32 and 0 <= y < 32:
                if dist == 3: p[x,y] = bone_outline
                elif dx < 0 or dy > 0: p[x,y] = bone_dark
                else: p[x,y] = bone_light
meat_dark = (58, 18, 58)
meat_mid = (90, 30, 75)
meat_light = (120, 45, 95)
meat_highlight = (145, 60, 110)
meat_outline = (35, 10, 35)
for y in range(4, 20):
    for x in range(12, 28):
        cx2, cy2 = 20, 12
        dx2 = (x - cx2) / 7.5
        dy2 = (y - cy2) / 7.0
        dist = dx2*dx2 + dy2*dy2
        if dist < 1.0:
            edge = 1.0 - dist
            shade = (x - 12) / 16.0 * 0.3 + (20 - y) / 16.0 * 0.4
            shade = max(0, min(1, shade + 0.3))
            if edge < 0.15: p[x,y] = meat_outline + (255,)
            elif shade > 0.7: p[x,y] = meat_highlight + (255,)
            elif shade > 0.45: p[x,y] = meat_light + (255,)
            elif shade > 0.2: p[x,y] = meat_mid + (255,)
            else: p[x,y] = meat_dark + (255,)
            if random.random() < 0.08 and edge > 0.2:
                p[x,y] = (30, random.randint(80, 140), 50, 255)
for i in range(3):
    sx, sy = 15 + i * 4, 9 + i * 2
    for j in range(3):
        x2 = sx+j
        if 0 <= x2 < 32 and 0 <= sy < 32 and p[x2,sy][3] > 0:
            p[x2,sy] = (140, 100, 120, 255)
img.save(OUT + 'strange_meat.png')
print('strange_meat done')

# 2. LIVING DIVINING ROD
img = Image.new('RGBA', (32, 32), (0,0,0,0))
p = img.load()
wood_dark = (60, 38, 22, 255)
wood_mid = (90, 58, 34, 255)
wood_light = (120, 78, 48, 255)
wood_highlight = (145, 100, 65, 255)
for y in range(18, 30):
    for dx in range(-1, 2):
        x = 15 + dx
        if dx == -1: p[x,y] = wood_dark
        elif dx == 1: p[x,y] = wood_highlight
        else: p[x,y] = wood_mid
        if (y * 3 + x * 7) % 5 == 0 and dx == 0: p[x,y] = wood_light
for i in range(12):
    bx, by = 14 - i, 17 - i
    if by < 2: break
    for dx in range(-1, 1):
        x = bx+dx
        if 0 <= x < 32 and 0 <= by < 32:
            p[x,by] = wood_dark if dx == -1 else wood_mid
for i in range(12):
    bx, by = 16 + i, 17 - i
    if by < 2: break
    for dx in range(0, 2):
        x = bx+dx
        if 0 <= x < 32 and 0 <= by < 32:
            p[x,by] = wood_highlight if dx == 1 else wood_mid
for tip_x, tip_y in [(3, 6), (27, 6)]:
    for ddx in range(-3, 4):
        for ddy in range(-3, 4):
            dist = math.sqrt(ddx*ddx + ddy*ddy)
            x, y = tip_x+ddx, tip_y+ddy
            if 0 <= x < 32 and 0 <= y < 32:
                if dist < 1.0: p[x,y] = (255, 255, 255, 255)
                elif dist < 1.8: p[x,y] = (100, 255, 250, 255)
                elif dist < 2.5: p[x,y] = (0, 210, 200, 200)
                elif dist < 3.2: p[x,y] = (0, 160, 150, 100)
for dx in range(-1, 2):
    for dy in range(-1, 2):
        x, y = 15+dx, 17+dy
        if 0 <= x < 32 and 0 <= y < 32: p[x,y] = wood_light
for vy in [21, 24, 27]:
    for dx in range(-2, 3):
        x = 15 + dx
        if 0 <= x < 32:
            if abs(dx) == 2: p[x,vy] = (30, 120, 40, 180)
            elif abs(dx) == 1: p[x,vy] = (45, 160, 55, 220)
img.save(OUT + 'living_divining_rod.png')
print('living_divining_rod done')

# 3. ABSORPTION ORB
img = Image.new('RGBA', (32, 32), (0,0,0,0))
p = img.load()
cx, cy = 15.5, 15.5
radius = 11.0
for y in range(32):
    for x in range(32):
        dx3 = x - cx
        dy3 = y - cy
        dist = math.sqrt(dx3*dx3 + dy3*dy3)
        if dist <= radius:
            norm = dist / radius
            if norm < 0.3:
                r = int(200 + 55 * (0.3 - norm) / 0.3)
                g = int(120 + 80 * (0.3 - norm) / 0.3)
                b = int(220 + 35 * (0.3 - norm) / 0.3)
            elif norm < 0.6:
                t = (norm - 0.3) / 0.3
                r, g, b = int(200 - 60*t), int(120 - 70*t), int(220 - 30*t)
            elif norm < 0.85:
                t = (norm - 0.6) / 0.25
                r, g, b = int(140 - 70*t), int(50 - 30*t), int(190 - 60*t)
            else:
                t = (norm - 0.85) / 0.15
                r, g, b = int(70 - 30*t), int(20 - 10*t), int(130 - 60*t)
            light = (-dx3 - dy3) / (radius * 2) + 0.5
            light = max(0, min(1, light))
            r = int(r * (0.7 + 0.3 * light))
            g = int(g * (0.7 + 0.3 * light))
            b = int(b * (0.7 + 0.3 * light))
            alpha = 255 if norm <= 0.9 else int(255 * (1.0 - norm) / 0.1)
            p[x,y] = (min(255,r), min(255,g), min(255,b), alpha)
for ddx in range(-3, 4):
    for ddy in range(-3, 4):
        dist = math.sqrt(ddx*ddx + ddy*ddy)
        hx, hy = 11+ddx, 10+ddy
        if 0 <= hx < 32 and 0 <= hy < 32:
            if dist < 1.5: p[hx,hy] = (255, 255, 255, 255)
            elif dist < 2.5: p[hx,hy] = (240, 210, 255, 220)
            elif dist < 3.5:
                old = p[hx,hy]
                if old[3] > 0:
                    p[hx,hy] = (min(255,old[0]+60), min(255,old[1]+40), min(255,old[2]+30), old[3])
for angle_deg in range(0, 360, 45):
    angle = math.radians(angle_deg)
    for r_dist in range(3, 8):
        sx = int(cx + r_dist * math.cos(angle))
        sy = int(cy + r_dist * math.sin(angle))
        if 0 <= sx < 32 and 0 <= sy < 32 and p[sx,sy][3] > 0:
            old = p[sx,sy]
            p[sx,sy] = (min(255,old[0]+20), old[1], min(255,old[2]+25), old[3])
img.save(OUT + 'absorption_orb.png')
print('absorption_orb done')

# 4. CERULEAN INGOT
img = Image.new('RGBA', (32, 32), (0,0,0,0))
p = img.load()
outline = (15, 60, 120, 255)
top_light = (135, 206, 250, 255)
top_mid = (100, 170, 230, 255)
front_mid = (50, 120, 200, 255)
front_dark = (30, 90, 170, 255)
highlight = (180, 225, 255, 255)
for y in range(8, 15):
    row = y - 8
    x_start = 6 + row
    x_end = 26 + row
    for x in range(x_start, min(x_end, 32)):
        t = (x - x_start) / max(1, x_end - x_start - 1)
        if row == 0 or row == 6 or x == x_start or x == x_end - 1:
            p[x,y] = outline
        elif row <= 2:
            p[x,y] = lerp(top_mid, top_light, t) + (255,)
        elif row <= 4:
            p[x,y] = lerp(top_light, top_mid, abs(t-0.5)*2) + (255,)
        else:
            p[x,y] = (top_mid if t < 0.5 else top_light) + (255,)
for y in range(15, 26):
    for x in range(12, 32):
        if y == 25 or x == 12 or x == 31:
            p[x,y] = outline
        else:
            t_vert = (y - 15) / 10.0
            t_horiz = (x - 12) / 19.0
            base = lerp(front_mid, front_dark, t_vert)
            if t_horiz < 0.15:
                base = lerp(highlight[:3], base, t_horiz / 0.15)
            p[x,y] = base + (255,)
for i in range(4):
    x, y = 7+i, 9+i
    if 0 <= x < 32 and 0 <= y < 32: p[x,y] = highlight
for y in range(16, 25):
    for x in range(14, 30):
        if p[x,y][3] > 0 and (x*7+y*13) % 11 == 0:
            old = p[x,y]
            p[x,y] = (min(255,old[0]+15), min(255,old[1]+15), min(255,old[2]+15), 255)
img.save(OUT + 'cerulean_ingot.png')
print('cerulean_ingot done')

# 5. CRYSTALLINE SHARD
img = Image.new('RGBA', (32, 32), (0,0,0,0))
p = img.load()
c_outline = (0, 80, 90, 255)
c_dark = (0, 140, 150, 255)
c_mid = (60, 200, 210, 255)
c_light = (140, 240, 245, 255)
c_white = (230, 255, 255, 255)
for y in range(3, 28):
    t = (y - 3) / 24.0
    if t < 0.1: w = int(1 + t * 20)
    elif t < 0.5: w = int(3 + (t - 0.1) * 10)
    elif t < 0.8: w = int(7 - (t - 0.5) * 8)
    else: w = max(0, int(5 - (t - 0.8) * 20))
    w = max(0, min(w, 10))
    cx4 = 16
    for dx in range(-w, w+1):
        x = cx4 + dx
        if 0 <= x < 32:
            if dx < -w + 1 or dx > w - 1:
                p[x,y] = c_outline
            elif dx < 0:
                p[x,y] = lerp(c_mid, c_dark, abs(dx)/max(1,w)) + (255,)
            else:
                p[x,y] = lerp(c_mid, c_light, dx/max(1,w)) + (255,)
    if w > 0 and (y == 3 or y == 27):
        for dx2 in range(-w, w+1):
            xx = cx4 + dx2
            if 0 <= xx < 32: p[xx, y] = c_outline
for y in range(5, 26):
    if p[16,y][3] > 0: p[16,y] = c_light
    if 0 <= 15 < 32 and p[15,y][3] > 0:
        p[15,y] = lerp(c_mid, c_light, 0.5) + (255,)
for y in range(6, 15):
    for x in range(17, 20):
        if 0 <= x < 32 and p[x,y][3] > 0:
            p[x,y] = c_white if (x+y) % 3 == 0 else c_light
for y in range(18, 28):
    t = (y - 18) / 9.0
    w2 = int(2 * (1.0 - t))
    for dx in range(-w2, w2+1):
        x = 22 + dx
        if 0 <= x < 32 and 0 <= y < 32:
            if abs(dx) == w2: p[x,y] = c_outline
            else: p[x,y] = lerp(c_light, c_mid, t) + (255,)
img.save(OUT + 'crystalline_shard.png')
print('crystalline_shard done')

# 6. SPECTRAL SILK
img = Image.new('RGBA', (32, 32), (0,0,0,0))
p = img.load()
silk_white = (240, 240, 255)
silk_light = (210, 215, 240)
silk_mid = (180, 185, 220)
silk_dim = (150, 155, 200)
binding = (120, 100, 160)
binding_dark = (80, 65, 120)
strand_offsets = [-5, -3, -1, 1, 3, 5, 7]
for si, sx_off in enumerate(strand_offsets):
    base_x = 16 + sx_off
    alpha = 200 if si % 2 == 0 else 160
    color = silk_white if si % 3 == 0 else (silk_light if si % 3 == 1 else silk_mid)
    for y in range(3, 29):
        wave = math.sin((y + si * 2) * 0.6) * 1.5
        x = int(base_x + wave)
        if 0 <= x < 32 and 0 <= y < 32:
            p[x,y] = color + (alpha,)
            if x+1 < 32: p[x+1,y] = silk_dim + (max(0, alpha - 40),)
for y in range(13, 19):
    for x in range(9, 24):
        has_strand = False
        for idx2, sx_off2 in enumerate(strand_offsets):
            bx = 16 + sx_off2
            wave2 = math.sin((y + idx2 * 2) * 0.6) * 1.5
            if abs(x - int(bx + wave2)) <= 1:
                has_strand = True
                break
        if has_strand:
            if y == 13 or y == 18: p[x,y] = binding_dark + (230,)
            else: p[x,y] = binding + (220,)
for y in range(2, 5):
    for x in range(8, 25):
        if random.random() < 0.3: p[x,y] = silk_white + (80 + random.randint(0, 60),)
for y in range(27, 30):
    for x in range(8, 25):
        if random.random() < 0.3: p[x,y] = silk_white + (60 + random.randint(0, 50),)
for _ in range(8):
    sx, sy = random.randint(10, 22), random.randint(5, 26)
    if p[sx,sy][3] > 0: p[sx,sy] = (255, 255, 255, 255)
img.save(OUT + 'spectral_silk.png')
print('spectral_silk done')

# 7. UMBRA INGOT
img = Image.new('RGBA', (32, 32), (0,0,0,0))
p = img.load()
u_outline = (10, 10, 15, 255)
u_top_light = (80, 75, 90, 255)
u_top_mid = (60, 56, 72, 255)
u_front_mid = (42, 40, 55, 255)
u_front_dark = (28, 26, 38, 255)
u_highlight = (100, 90, 115, 255)
u_purple = (70, 45, 90, 255)
for y in range(8, 15):
    row = y - 8
    x_start = 6 + row
    x_end = 26 + row
    for x in range(x_start, min(x_end, 32)):
        t = (x - x_start) / max(1, x_end - x_start - 1)
        if row == 0 or row == 6 or x == x_start or x == x_end - 1:
            p[x,y] = u_outline
        elif row <= 2:
            p[x,y] = lerp(u_top_mid, u_top_light, t) + (255,)
        elif row <= 4:
            p[x,y] = lerp(u_top_light, u_top_mid, abs(t-0.5)*2) + (255,)
        else:
            p[x,y] = u_top_mid + (255,)
for y in range(15, 26):
    for x in range(12, 32):
        if y == 25 or x == 12 or x == 31:
            p[x,y] = u_outline
        else:
            t_vert = (y - 15) / 10.0
            t_horiz = (x - 12) / 19.0
            base = lerp(u_front_mid, u_front_dark, t_vert)
            if t_horiz < 0.15:
                base = lerp(u_highlight[:3], base, t_horiz / 0.15)
            p[x,y] = base + (255,)
for y in range(16, 25):
    for x in range(14, 30):
        if p[x,y][3] > 0 and (x*5+y*11) % 17 == 0: p[x,y] = u_purple
for i in range(4):
    x, y = 7+i, 9+i
    if 0 <= x < 32 and 0 <= y < 32: p[x,y] = u_highlight
for mx, my in [(18, 19), (22, 20), (26, 18)]:
    for dx in range(-1, 2):
        for dy in range(-1, 2):
            if abs(dx)+abs(dy) <= 1:
                px2, py2 = mx+dx, my+dy
                if 0 <= px2 < 32 and 0 <= py2 < 32 and p[px2,py2][3] > 0:
                    p[px2,py2] = u_purple
img.save(OUT + 'umbra_ingot.png')
print('umbra_ingot done')
print('All 7 textures regenerated at 32x32!')
