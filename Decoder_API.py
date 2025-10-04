import serial
import threading
import time
import Decoder_UI

serial_port = None
running = False
thread = None
data_callback = None

def connect(port, baudrate):
    global serial_port
    try:
        serial_port = serial.Serial(port, baudrate, timeout=1)
        return True, f"Connected to {port} at {baudrate} baud"
    except Exception as e:
        return False, f"Connection error: {e}"

def start():
    global running, thread
    if not running:
        running = True
        thread = threading.Thread(target=read_loop)
        thread.daemon = True
        thread.start()
        print("Reading thread started")

def read_loop():
    global running
    while running:
        try:
            if serial_port and serial_port.in_waiting:
                data = serial_port.readline()
                if data_callback and data:
                    data_callback(data)
                    print(data)
        except Exception as e:
            print(f"Read error: {e}")
            time.sleep(0.1)

def stop_reading():
    global running, thread
    running = False
    if thread:
        thread.join(timeout=1.0)
    print("Reading stopped")

def write(message: str):
    if serial_port and serial_port.is_open:
        serial_port.write(message.encode())

def close():
    global serial_port
    stop_reading()
    if serial_port and serial_port.is_open:
        serial_port.close()
        print("Serial port closed")

def set_callback(callback_function):
    global data_callback
    data_callback = callback_function

def is_connected():
    return serial_port and serial_port.is_open