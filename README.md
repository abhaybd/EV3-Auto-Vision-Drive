# FLL-Vision-Drive

This is a Lego Mindstorms EV3 Robot, using LeJOS EV3 to run Java, intelligently navigating using a webcam, powered by deep learning.

It uses a YOLO network, a specialized type of convolutional neural network to detect and find the location of objects within an image.

To save processing time, the tiny-YOLO network is used, which is smaller, lighter, and faster, however it's less accurate.

The webcam grabs an image, and sends it over the wifi using TCP to the computer. The computer then runs a tiny-YOLO network, trained on the pascal dataset, to recognize targets.

The bounding boxes of the targets are sent back to the robot, and then the robot moves towards the targets.

[LeJOS EV3](https://lejos.sourceforge.io/ev3.php) is used to program the EV3.

The tiny-YOLO network and test/inference code is gotten from [allanzelener's YAD2K repository.](https://github.com/allanzelener/YAD2K)
