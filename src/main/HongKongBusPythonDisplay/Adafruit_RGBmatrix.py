class Adafruit_RGBmatrix:
    horiz = 0
    vert = 0

    def __init__(self, horiz=0, vert=0):
        self.horiz = horiz
        self.vert = vert

    def __str__(self):
        return "Horiz " + str(self.horiz) + " - Vert " + str(self.vert)

    def __repr__(self):
        return "Horiz " + str(self.horiz) + " - Vert " + str(self.vert)

    def Clear(self):
        print("Fake Clearing")

    def SetImage(self, imageId=0, param1=0, param2=0):
        print("Fake SetImage")
