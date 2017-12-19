import socket as s
import struct
import numpy as np
import threading
import yolo

port = 4444

serversocket = s.socket()
serversocket.bind((s.gethostname(), port))
serversocket.listen()

def udp_thread():
    udp_sock = s.socket(s.AF_INET, s.SOCK_DGRAM)
    udp_sock.bind((s.gethostname(),4445))
    print('Waiting for udp message!')
    while True:
        msg, addr = udp_sock.recvfrom(256)
        print('Recieved udp message from {}'.format(addr))
        udp_sock.sendto(msg, addr)
        
thread = threading.Thread(target = udp_thread, args=())
thread.daemon = True
thread.start()

print('Waiting for connection...')
socket, addr = serversocket.accept()
print('Connected!')

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
    img = np.zeros((height, width, 3), dtype=np.float32)
    for i in range(0,len(arr),4):
        y1,u,y2,v = [int(a) for a in arr[i:i+4]]
        x = (i % (width * 2)) // 2
        y = i // (width * 2)
        img[y,x] = yuv_to_rgb(y1,u,v)
        img[y,x+1] = yuv_to_rgb(y2,u,v)
    return img;

def clamp(num, low, high):
    return min(max(num,low),high)

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

images = []
while True:
    img_width = recieve_int(socket)
    img_height = recieve_int(socket)
    
    # Recieve image bytes and convert to np array
    msg_len = recieve_int(socket)
    raw_data = recieve(socket, msg_len)
    img_data = bytes_to_img(raw_data, img_width, img_height)
    
    images.append(img_data/255)
    
    bounds = yolo.get_pred(img_data, 'person')
    print('Pred: {}'.format(bounds))