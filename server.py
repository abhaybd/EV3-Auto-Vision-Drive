import socket as s
import struct

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

while True:
    msg_len = recieve(socket, 4)
    msg_len = struct.unpack('!i', msg_len)[0]
    img_data = recieve(socket, msg_len)
    