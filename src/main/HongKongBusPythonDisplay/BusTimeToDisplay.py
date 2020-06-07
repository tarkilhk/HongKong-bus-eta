import dateutil.parser as dp

def formatTohhmm(arrivalTime):
    parsedDate = dp.parse(arrivalTime)
    return parsedDate.strftime('%H:%M')


class BusTimeToDisplay:
    busNumber = '0'
    arrivalTime = ''
    distance = ''
    isAnError = False
    color = (0, 0, 0)
    arrivalTime24H = ''

    def __init__(self, busNumber='0', arrivalTime='', distance='', isAnError=False, color=(0,0,0)):
        self.busNumber = busNumber
        self.arrivalTime = formatTohhmm(arrivalTime)
        #If 00 is not found, then it seems like it returns an empty string !! ?????
        self.arrivalTime24H = arrivalTime.replace('00:', '24:')
        ## "Distance : 1.23km" -> "1.23km" -> First 5 characters
        self.distance = distance.replace('Distance: ', '')[:5]
        self.isAnError = isAnError
        self.color = color
        
    def __str__(self):
        return "Bus " + self.busNumber + " @ " + self.distance + " - " + self.arrivalTime
    
    def __repr__(self):
        return "Bus " + self.busNumber + " @ " + self.distance + " - " + self.arrivalTime
