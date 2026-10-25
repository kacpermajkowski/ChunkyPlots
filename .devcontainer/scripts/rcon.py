#!/usr/bin/env python3
"""
Simple standalone RCON client for Minecraft / Paper servers.
Uses standard Python library (no external packages required).
"""
import socket
import struct
import sys
import os

SERVER_HOST = os.environ.get("RCON_HOST", "127.0.0.1")
SERVER_PORT = int(os.environ.get("RCON_PORT", "25575"))
SERVER_PASS = os.environ.get("RCON_PASSWORD", "localdevpassword")

def send_packet(sock, req_id, req_type, payload):
    body = payload.encode("utf-8")
    length = 4 + 4 + len(body) + 2
    sock.sendall(struct.pack("<iii", length, req_id, req_type) + body + b"\x00\x00")

def read_packet(sock):
    header = sock.recv(4)
    if not header or len(header) < 4:
        return -1, -1, ""
    length = struct.unpack("<i", header)[0]
    data = b""
    while len(data) < length:
        chunk = sock.recv(length - len(data))
        if not chunk:
            break
        data += chunk
    if len(data) < 8:
        return -1, -1, ""
    req_id, req_type = struct.unpack("<ii", data[:8])
    payload = data[8:-2].decode("utf-8", errors="replace") if length > 10 else ""
    return req_id, req_type, payload

def run_command(command):
    try:
        with socket.create_connection((SERVER_HOST, SERVER_PORT), timeout=7) as sock:
            # Login (type 3)
            send_packet(sock, 1, 3, SERVER_PASS)
            auth_id, _, _ = read_packet(sock)
            if auth_id == -1:
                print("Error: RCON authentication failed (invalid password)", file=sys.stderr)
                sys.exit(1)
            # Execute command (type 2)
            send_packet(sock, 2, 2, command)
            _, _, response = read_packet(sock)
            if response:
                print(response.strip())
            else:
                print("(Command executed, no output returned)")
    except ConnectionRefusedError:
        print(f"Error: Dev server is stopped (connection refused at {SERVER_HOST}:{SERVER_PORT})", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"Error connecting to RCON: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(f"Usage: {sys.argv[0]} <minecraft-command>", file=sys.stderr)
        sys.exit(1)
    cmd = " ".join(sys.argv[1:])
    run_command(cmd)

