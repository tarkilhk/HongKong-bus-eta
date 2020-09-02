# -*- coding: UTF-8 -*-
import json
import logging
import os
import threading
from logging.handlers import RotatingFileHandler

import psutil
import requests
import sys
import time
from PIL import Image
from PIL import ImageDraw
from PIL import ImageFont

import BusTimeToDisplayNoDistance

# Define running folder
if os.name == 'nt':
    ProgramFolder = 'C:\\Users\\rober\\ideaProjects\\HongKongBus\\src\\main\\HongKongBusPythonDisplay\\'
    from Adafruit_RGBmatrix import Adafruit_RGBmatrix
else:
    ProgramFolder = "/home/pi/Downloads/rpi-rgb-led-matrix-master/"
    from rgbmatrix import Adafruit_RGBmatrix

# Logging mechanism
log = logging.getLogger('RotatingLogs')
myHandler = RotatingFileHandler(ProgramFolder + "CityBus-eta.log", maxBytes=20000000, backupCount=5)
myHandler.setLevel(logging.INFO)
myHandler.setFormatter(logging.Formatter('(%(threadName)s) %(asctime)s %(message)s'))
log.addHandler(myHandler)
log.setLevel(logging.INFO)

# Define colors
DarkRed = (50, 0, 0)
LightRed = (100, 0, 0)
DarkWhite = (100, 100, 100)
PreciousCyan = (102, 245, 173)

# Root URL
rootUrl = "https://hong-kong-bus-eta.herokuapp.com"
# rootUrl = "http://127.0.0.1:5000"

# Global variable to be used between the 2 threads
NextArrivalTimes = []


def GetDisplayColor(busNumber):
    displayColor = (0, 0, 0)
    if busNumber == '11' or busNumber == '-1':
        displayColor = DarkRed
    if busNumber == '511':
        displayColor = LightRed
    return displayColor


def GetCityBusProcess():
    global log
    for myProcess in psutil.process_iter():
        if 'sudo' in myProcess.cmdline():
            if 'python' in myProcess.cmdline():
                if ProgramFolder + 'CityBus-eta.py' in myProcess.cmdline() or 'CityBus-eta.py' in myProcess.cmdline():
                    if myProcess.pid == os.getppid():
                        log.debug(
                            "Found sudo, python, and CityBus-eta.py process, but only current process, no already "
                            "running one")
                    else:
                        log.debug("Found CityBuys-eta.py process")
                        return myProcess
                else:
                    log.debug("Found sudo and python, but not CityBus-eta")
            else:
                log.debug("Found sudo, but not python")
    return None


def RefreshBusTimeData():
    global NextArrivalTimes
    global log

    # sessionIdWhichIKnowEqualsOne = "222"
    list_of_bus_stops = requests.get(rootUrl + '/users/favourite-stops?userName=pi').json()
    if len(list_of_bus_stops) != 1:
        log.error("pi user should't have more than 1 bus stop defined, exiting ...")
        exit(0)
    busStopId = list_of_bus_stops[0]['busStopId']
    log.info("Retrieved 1 bus stops for user 'pi' with id %s", busStopId)

    FoundArrivalTimes = []
    while True:
        try:
            log.info("Beginning of RefreshBusTimeData While True Loop")
            FoundArrivalTimes = []

            timeout = 30
            start = time.time()
            body = []

            response = requests.get(rootUrl + '/bus-eta/bus-stop?stopId=' + str(busStopId), timeout=timeout,
                                    stream=True)
            for chunk in response.iter_content(1024):  # Adjust this value to provide more or less granularity.
                body.append(chunk)
                if time.time() > (start + timeout):
                    log.error(
                        "Getting data from backend took more than {} seconds, breaking operation".format(str(timeout)))
                    break  # You can set an error value here as well if you want.
            content = b''.join(body)

            if response.status_code == 200:
                data = json.loads(content.decode())
                for obj in data.get('etas'):
                    FoundArrivalTimes.append(
                        BusTimeToDisplayNoDistance.BusTimeToDisplay(obj.get('busNumber'), obj.get('eta'), "",
                                                                    data.get('isError'),
                                                                    GetDisplayColor(obj.get('busNumber'))))
                log.info("RefreshBusTimeData successfully retrieved %s elements", len(FoundArrivalTimes))
            else:
                log.error("Received Status Code = %s when trying to refresh NextBusTimes, skipping to next loop",
                          response.status_code)
        except requests.ConnectionError as err:
            FoundArrivalTimes.append(
                BusTimeToDisplayNoDistance.BusTimeToDisplay('0', 'OFFLN', 'OFFLN', DarkRed)
            )
            log.error('Connection error while refreshing BusTimeData')
            log.exception(err)
            time.sleep(30)
        except requests.exceptions.Timeout as err:
            FoundArrivalTimes.append(
                BusTimeToDisplayNoDistance.BusTimeToDisplay('0', 'TMOUT', 'TMOUT', DarkRed)
            )
            log.error('Time out while refreshing BusTimeData')
            log.exception(err)
            time.sleep(30)
        except requests.exceptions.RequestException as err:
            log.error("Couldn't GET the BusTimeData")
            log.exception(err)
        except Exception as err:
            log.error("Unmanaged exception caught during GET nextBusTimesFor - response code %s",
                      str(err))
        finally:
            NextArrivalTimes = sorted(FoundArrivalTimes,
                                      key=lambda myBusTime: myBusTime.arrivalTime.replace("00:", "24:"))
            time.sleep(30)


def KeepDisplayUpdated():
    global NextArrivalTimes
    global log

    # Rows and chain length are both required parameters:
    matrix = Adafruit_RGBmatrix(32, 2)

    localNextArrivalTimesToAvoidConcurrencyIssues = []

    log.info("Starting KeepDisplayUpdated")
    fontSize = 10
    # font = ImageFont.truetype("/usr/share/fonts/truetype/freefont/FreeSans.ttf", fontSize)
    # font = ImageFont.truetype("/home/pi/Downloads/5by7.ttf", fontSize)
    # font = ImageFont.truetype("/home/pi/Downloads/ufonts.com_subway-ticker.ttf", fontSize)
    font = ImageFont.truetype(ProgramFolder + "LEDCounter7.ttf", fontSize)
    displaying = 'arrivalTime'

    while True:
        myImage = Image.new("RGB", (64, 32), "black")
        myDraw = ImageDraw.Draw(myImage)
        myDraw.text((19, 0), time.strftime('%H:%M', time.localtime()), DarkWhite, font=font)

        localNextArrivalTimesToAvoidConcurrencyIssues = NextArrivalTimes
        for nextArrivalTime in NextArrivalTimes:
            log.debug(nextArrivalTime)

        if len(localNextArrivalTimesToAvoidConcurrencyIssues) == 0:
            myDraw.text((3, fontSize), "No bus", DarkRed, font=font)
        elif len(localNextArrivalTimesToAvoidConcurrencyIssues) == 1:
            if localNextArrivalTimesToAvoidConcurrencyIssues[0].isAnError:
                # myDraw.text((1, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[0].arrivalTime,
                myDraw.text((3, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[0].arrivalTime,
                            localNextArrivalTimesToAvoidConcurrencyIssues[0].color, font=font)
            else:
                if displaying == 'arrivalTime':
                    myDraw.text((3, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[0].arrivalTime,
                                localNextArrivalTimesToAvoidConcurrencyIssues[0].color, font=font)
                else:
                    myDraw.text((3, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[0].distance,
                                localNextArrivalTimesToAvoidConcurrencyIssues[0].color, font=font)
        elif len(localNextArrivalTimesToAvoidConcurrencyIssues) == 2:
            if displaying == 'arrivalTime':
                myDraw.text((3, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[0].arrivalTime,
                            localNextArrivalTimesToAvoidConcurrencyIssues[0].color, font=font)
                myDraw.text((35, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[1].arrivalTime,
                            localNextArrivalTimesToAvoidConcurrencyIssues[1].color, font=font)
            else:
                myDraw.text((3, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[0].distance,
                            localNextArrivalTimesToAvoidConcurrencyIssues[0].color, font=font)
                myDraw.text((35, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[1].distance,
                            localNextArrivalTimesToAvoidConcurrencyIssues[1].color, font=font)
        elif len(localNextArrivalTimesToAvoidConcurrencyIssues) == 3:
            if displaying == 'arrivalTime':
                myDraw.text((3, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[0].arrivalTime,
                            localNextArrivalTimesToAvoidConcurrencyIssues[0].color, font=font)
                myDraw.text((35, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[1].arrivalTime,
                            localNextArrivalTimesToAvoidConcurrencyIssues[1].color, font=font)
                myDraw.text((3, fontSize * 2), localNextArrivalTimesToAvoidConcurrencyIssues[2].arrivalTime,
                            localNextArrivalTimesToAvoidConcurrencyIssues[2].color, font=font)
            else:
                myDraw.text((3, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[0].distance,
                            localNextArrivalTimesToAvoidConcurrencyIssues[0].color, font=font)
                myDraw.text((35, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[1].distance,
                            localNextArrivalTimesToAvoidConcurrencyIssues[1].color, font=font)
                myDraw.text((3, fontSize * 2), localNextArrivalTimesToAvoidConcurrencyIssues[2].distance,
                            localNextArrivalTimesToAvoidConcurrencyIssues[2].color, font=font)
        else:
            if displaying == 'arrivalTime':
                myDraw.text((3, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[0].arrivalTime,
                            localNextArrivalTimesToAvoidConcurrencyIssues[0].color, font=font)
                myDraw.text((35, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[1].arrivalTime,
                            localNextArrivalTimesToAvoidConcurrencyIssues[1].color, font=font)
                myDraw.text((3, fontSize * 2), localNextArrivalTimesToAvoidConcurrencyIssues[2].arrivalTime,
                            localNextArrivalTimesToAvoidConcurrencyIssues[2].color, font=font)
                myDraw.text((35, fontSize * 2), localNextArrivalTimesToAvoidConcurrencyIssues[3].arrivalTime,
                            localNextArrivalTimesToAvoidConcurrencyIssues[3].color, font=font)
            else:
                myDraw.text((3, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[0].distance,
                            localNextArrivalTimesToAvoidConcurrencyIssues[0].color, font=font)
                myDraw.text((35, fontSize), localNextArrivalTimesToAvoidConcurrencyIssues[1].distance,
                            localNextArrivalTimesToAvoidConcurrencyIssues[1].color, font=font)
                myDraw.text((3, fontSize * 2), localNextArrivalTimesToAvoidConcurrencyIssues[2].distance,
                            localNextArrivalTimesToAvoidConcurrencyIssues[2].color, font=font)
                myDraw.text((35, fontSize * 2), localNextArrivalTimesToAvoidConcurrencyIssues[3].distance,
                            localNextArrivalTimesToAvoidConcurrencyIssues[3].color, font=font)

        log.debug("DisplayUpdated : Before RGBMatrix.Clear")
        matrix.Clear()

        log.debug("DisplayUpdated : Before RGBMatrix.SetImage")
        matrix.SetImage(myImage.im.id, 0, 0)

        if displaying == 'arrivalTime':
            log.debug("KeepDisplayUpdated sleeping for 3 seconds")
            time.sleep(5)
            # if distance comes again one day, just uncomment  the 3 lines of code below (and remove time.sleep(5) above)
            # time.sleep(3)
            # myLogger.debug("KeepDisplayUpdated arrivalTime SLEEP DONE")
            # displaying = 'distance'
        else:
            log.debug("KeepDisplayUpdated sleeping for 1 second")
            time.sleep(1)
            log.debug("KeepDisplayUpdated distance SLEEP DONE")
            displaying = 'arrivalTime'


def ThreadManager():
    global log

    # wakes up every 5 minutes to check if the 2 threads are running
    # restarts missing ones if needed

    print("Arrived in ThreadManager")
    log.warning("Arrived in ThreadManager")

    myRefreshBusTimeDataThread = threading.Thread(name='RefreshBusTimeData', target=RefreshBusTimeData)
    myKeepDisplayUpdatedThread = threading.Thread(name='KeepDisplayUpdated', target=KeepDisplayUpdated)

    while True:
        print("ThreadManager While True Loop started")
        if not myRefreshBusTimeDataThread.isAlive():
            myRefreshBusTimeDataThread.start()
            log.info('ThreadManager : RefreshBusTimeData started')
        # if not myKeepDisplayUpdatedThread.isAlive() and os.name != 'nt':
        if not myKeepDisplayUpdatedThread.isAlive():
            myKeepDisplayUpdatedThread.start()
            log.info('ThreadManager : KeepDisplayUpdated started')
        time.sleep(30)


try:
    # Create 1 thread as follows    
    myThreadManagerThread = threading.Thread(name='ThreadManager', target=ThreadManager)

    if os.name == 'nt':
        myThreadManagerThread.start()
        print("Main : CityBus NT started")
        log.warning("CityBus NT ThreadManager Started")
    else:
        myProcess = GetCityBusProcess()
        if len(sys.argv) < 2:
            print("Wrong arguments, you need to specify start or stop")
            log.error("Wrong argument, only start and stop are supported")
            sys.exit()

        # If process is running already
        if myProcess is not None:
            if sys.argv[1] == "start":
                print("CityBus is already running, cannot start a second time")
                log.error("Main : CityBus is already running, cannot start a second time")
            elif sys.argv[1] == "stop":
                log.warning("Main : CityBus stopping")
                myProcess.terminate()
                print("CityBus stopped")
            else:
                print("Wrong argument, only start and stop are supported")
                log.error("Main : Wrong argument, only start and stop are supported")
        else:
            if sys.argv[1] == "start":
                log.warning('Starting')
                myThreadManagerThread.start()
                print("CityBus started")
                log.warning("Main : CityBus ThreadManager Started")
            elif sys.argv[1] == "stop":
                print("CityBus is not running, cannot stop it")
                log.error("Main : CityBus is not running, cannot stop it")
            else:
                print("Wrong argument, only start and stop are supported")
                log.error("Main : Wrong argument, only start and stop are supported")
except:
    log.exception("Unable to run the program")
