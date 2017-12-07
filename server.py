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
    socket.sendall(struct.pack('!I',num))

def bytes_to_img(arr, width, height):
    assert width*height*3 == len(arr)
    arr = [int(b) for b in arr]
    img = []
    for i in range(0,len(arr),3):
        img.append(arr[i:i+3])
    img = np.array(img).reshape((width, height,3))
    return img;

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
    