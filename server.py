import socket as s
import struct
import numpy as np

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

def bytes_to_img(arr, width, height):
    assert width*height*3 == len(arr)
    arr = [int(b) for b in arr]
    img = []
    for i in range(0,len(arr),3):
        img.append(arr[i:i+3])
    img = np.array(img).reshape((width, height))

while True:
    img_width = recieve_int(socket)
    img_height = recieve_int(socket)
    msg_len = recieve_int(socket)
    img_data = recieve(socket, msg_len)
    img_data = bytes_to_img(img_data, img_width, img_height)
    