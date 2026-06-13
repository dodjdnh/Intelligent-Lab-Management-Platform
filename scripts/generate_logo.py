from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter


SIZE = 1024
BG = "#071426"
CYAN = "#56F0FF"
BLUE = "#2F6BFF"
LIGHT = "#D9FBFF"
NAVY = "#0D223F"


def hex_rgba(value, alpha=255):
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4)) + (alpha,)


def add_glow(base, shape_fn, color, blur=24, passes=3):
    glow = Image.new("RGBA", base.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(glow)
    shape_fn(draw, hex_rgba(color, 210))
    for _ in range(passes):
        glow = glow.filter(ImageFilter.GaussianBlur(blur))
    return Image.alpha_composite(base, glow)


def main():
    image = Image.new("RGBA", (SIZE, SIZE), hex_rgba(BG))
    draw = ImageDraw.Draw(image)

    cx = cy = SIZE // 2

    for i, alpha in enumerate((28, 20, 14)):
        r = 360 + i * 85
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=hex_rgba(CYAN, alpha), width=2)

    def outer_ring(d, fill):
        d.ellipse((152, 152, 872, 872), outline=fill, width=18)

    image = add_glow(image, outer_ring, CYAN, blur=18, passes=2)
    draw = ImageDraw.Draw(image)
    draw.ellipse((152, 152, 872, 872), outline=hex_rgba(CYAN, 245), width=10)
    draw.ellipse((195, 195, 829, 829), outline=hex_rgba(BLUE, 160), width=3)

    bottle = [
        (468, 248), (556, 248), (556, 334), (666, 588),
        (690, 710), (334, 710), (358, 588), (468, 334)
    ]

    def bottle_glow(d, fill):
        d.polygon(bottle, outline=fill, width=22)

    image = add_glow(image, bottle_glow, BLUE, blur=20, passes=2)
    draw = ImageDraw.Draw(image)
    draw.polygon(bottle, fill=hex_rgba(NAVY, 238), outline=hex_rgba(LIGHT, 255), width=10)

    liquid = [(390, 596), (634, 596), (658, 676), (366, 676)]
    draw.polygon(liquid, fill=hex_rgba(CYAN, 84))
    image = add_glow(image, lambda d, fill: d.polygon(liquid, fill=fill), CYAN, blur=22, passes=2)
    draw = ImageDraw.Draw(image)
    draw.line((404, 620, 622, 620), fill=hex_rgba(LIGHT, 220), width=4)

    bubbles = [
        (450, 552, 472, 574),
        (520, 524, 548, 552),
        (566, 566, 582, 582),
    ]
    for bubble in bubbles:
        draw.ellipse(bubble, fill=hex_rgba(LIGHT, 210), outline=hex_rgba(CYAN, 230), width=2)

    circuit_lines = [
        ((272, 490), (152, 490)),
        ((752, 490), (872, 490)),
        ((370, 760), (290, 840)),
        ((654, 760), (734, 840)),
        ((512, 132), (512, 64)),
    ]

    for start, end in circuit_lines:
        draw.line((*start, *end), fill=hex_rgba(CYAN, 255), width=8)
        for px, py in (start, end):
            image = add_glow(
                image,
                lambda d, fill, x=px, y=py: d.ellipse((x - 16, y - 16, x + 16, y + 16), fill=fill),
                CYAN,
                blur=12,
                passes=1,
            )
            draw = ImageDraw.Draw(image)
            draw.ellipse((px - 10, py - 10, px + 10, py + 10), fill=hex_rgba(LIGHT, 255))

    image = add_glow(
        image,
        lambda d, fill: d.arc((250, 250, 774, 774), start=210, end=330, fill=fill, width=10),
        CYAN,
        blur=16,
        passes=2,
    )
    draw = ImageDraw.Draw(image)
    draw.arc((250, 250, 774, 774), start=210, end=330, fill=hex_rgba(CYAN, 255), width=6)

    out = Path(__file__).resolve().parents[1] / "logo-tech-lab.png"
    image.save(out)
    print(out)


if __name__ == "__main__":
    main()
