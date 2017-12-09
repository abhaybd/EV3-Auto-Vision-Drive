import socket as s
import struct
import numpy as np
import yolo

port = 4444

serversocket = s.socket()
serversocket.bind((s.gethostname(), port))
serversocket.listen()

socket, addr = serversocket.accept()

def recieve(socket, msg_len):
    chunks = []
    bytes_recd = 0
    while bytes_recd < msg_len:
        chunk = socket.recv(min(msg_len - bytes_recd, 2048))
        if chunk == b'':
            raise RuntimeError("socket connection broken")
        chunks.append(chunk)
        bytes_recd = bytes_recd + len(chunk)
    return b''.join(chunks)

def recieve_int(socket):
    return struct.unpack('!i', recieve(socket, 4))[0]

def send_int(socket, num):
    socket.sendall(struct.pack('!i',num))

def bytes_to_img(arr, width, height):
    img = np.zeros((height, width,3), dtype=np.float32)
    for i in range(0,len(arr),4):
        y1,u,y2,v = [int(a) for a in arr[i:i+4]]
        x = (i % (width * 2)) / 2
        y = i / (width * 2)
        img[y,x] = yuv_to_rgb(y1,u,v)
        img[y,x+1] = yuv_to_rgb(y2,u,v)
    img = np.array(img).reshape((width, height,3))
    return img;

def clamp(num, low, high):
    return min(max(num,low),high)

# TODO: Figure out unwrapping and converting

def yuv_to_rgb(y,u,v):
    c = y-16
    d = u-128
    e = v-128
    r = (298*c+409*e+128)/256
    g = (298*c-100*d-208*e+128)/256
    b = (298*c+516*d+128)/256
    r = clamp(r,0,255)
    g = clamp(g,0,255)
    b = clamp(b,0,255)
    return r, g, b

while True:
    # Recieve image dimensions
    img_width = recieve_int(socket)
    img_height = recieve_int(socket)
    
    # Recieve image bytes and convert to np array
    msg_len = recieve_int(socket)
    global img_data
    img_data = recieve(socket, msg_len)
    img_data = bytes_to_img(img_data, img_width, img_height)
    
    # Recieve tuple of bounding box values from yolo network
    # Tuple is in the form (xmin, ymin, xmax, ymax)
    # Then send all the values
    bound_box = yolo.get_pred(img_data, 'person')
    for val in bound_box:
        send_int(socket, val)
    