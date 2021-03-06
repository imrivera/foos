from PIL import ImageFont, ImageDraw, Image
import sys


def makeImage(l):
    # most monospaced fonts have ugly zeroes use upper case O instead
    letter = l if l != '0' else 'O'
    mask = Image.open('circle_m.png')
    draw = ImageDraw.Draw(mask)

    image = Image.new("LA", (512, 512), "white")

    #f = "/usr/share/fonts/truetype/liberation/LiberationMono-Bold.ttf"
    f = "../../UbuntuMono-B_circle.ttf"
    font = ImageFont.truetype(f, 480)
    sx, sy = font.getsize(letter)
    #sy for ubuntumono seems to be off
    #sy = sy * 0.75
    ascent, descent = font.getmetrics()
    posx = 256 - sx / 2
    posy = 256 - ascent + sy / 2

    #posy = 0
    #draw.rectangle((0,ascent-sy,512, ascent), fill='red')
    #draw.rectangle((0,0,512, sy), fill='blue')
    draw.text((posx, posy), letter, font=font, fill=0)

    image.putalpha(mask)
    image.save("%s.png" % (l))


for letter in range(ord('0'), ord('9') + 1):
    letter = chr(letter)

    makeImage(letter)
