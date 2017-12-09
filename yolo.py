"""Run a YOLO_v2 style detection model on test images."""

import numpy as np
from keras import backend as K
from keras.models import load_model
from PIL import Image

from yad2k.models.keras_yolo import yolo_eval, yolo_head

global last_pred
last_pred = None

model_path = 'model_data/yolo.h5'
assert model_path.endswith('.h5'), 'Keras model must be a .h5 file.'
anchors_path = 'model_data/yolo_anchors.txt'
classes_path = 'model_data/pascal_classes.txt'
iou_threshold = 0.5
score_threshold = 0.3
sess = K.get_session()  # TODO: Remove dependence on Tensorflow session.

with open(classes_path) as f:
    class_names = f.readlines()
class_names = [c.strip() for c in class_names]

with open(anchors_path) as f:
    anchors = f.readline()
    anchors = [float(x) for x in anchors.split(',')]
    anchors = np.array(anchors).reshape(-1, 2)

yolo_model = load_model(model_path)

# Verify model, anchors, and classes are compatible
num_classes = len(class_names)
num_anchors = len(anchors)
# TODO: Assumes dim ordering is channel last
model_output_channels = yolo_model.layers[-1].output_shape[-1]
assert model_output_channels == num_anchors * (num_classes + 5), \
    'Mismatch between model and given anchor and class sizes. ' \
    'Specify matching anchors and classes with --anchors_path and ' \
    '--classes_path flags.'
print('{} model, anchors, and classes loaded.'.format(model_path))

# Check if model is fully convolutional, assuming channel last order.
model_image_size = yolo_model.layers[0].input_shape[1:3]
is_fixed_size = model_image_size != (None, None)

# Generate output tensor targets for filtered bounding boxes.
# TODO: Wrap these backend operations with Keras layers.
yolo_outputs = yolo_head(yolo_model.output, anchors, len(class_names))
input_image_shape = K.placeholder(shape=(2, ))
boxes, scores, classes = yolo_eval(
    yolo_outputs,
    input_image_shape,
    score_threshold=score_threshold,
    iou_threshold=iou_threshold)
def get_pred(image, target_class):
    if type(image) == np.ndarray:
        image = Image.fromarray(image)
    elif not issubclass(type(image),Image.Image):
        raise Exception('image must be of type PIL.Image.Image')
    if is_fixed_size:
        resized_image = image.resize(
            tuple(reversed(model_image_size)), Image.BICUBIC)
        image_data = np.array(resized_image, dtype='float32')
    else:
        # Due to skip connection + max pooling in YOLO_v2, inputs must have
        # width and height as multiples of 32.
        new_image_size = (image.width - (image.width % 32),
                          image.height - (image.height % 32))
        resized_image = image.resize(new_image_size, Image.BICUBIC)
        image_data = np.array(resized_image, dtype='float32')
        print(image_data.shape)

    image_data /= 255.
    image_data = np.expand_dims(image_data, 0)  # Add batch dimension.

    out_boxes, out_scores, out_classes = sess.run(
        [boxes, scores, classes],
        feed_dict={
            yolo_model.input: image_data,
            input_image_shape: [image.size[1], image.size[0]],
            K.learning_phase(): 0
        })
    print('Found {} boxes'.format(len(out_boxes)))
    
    preds = []

    for i, c in reversed(list(enumerate(out_classes))):
        predicted_class = class_names[c]
        box = out_boxes[i]
        score = out_scores[i]
        if predicted_class != target_class:
            continue
        label = '{} {:.2f}'.format(predicted_class, score)

        ymin, xmin, ymax, xmax = box
        ymin = max(0, np.floor(ymin + 0.5).astype('int32'))
        xmin = max(0, np.floor(xmin + 0.5).astype('int32'))
        ymax = min(image.size[1], np.floor(ymax + 0.5).astype('int32'))
        xmax = min(image.size[0], np.floor(xmax + 0.5).astype('int32'))
        print(label, (xmin, ymin), (xmax, ymax))
        preds.append([predicted_class, score, (xmin, ymin, xmax, ymax)])
    if len(preds) == 0:
        return -1,-1,-1,-1
    global last_pred
    if type(last_pred) != np.ndarray:
        biggest = max(preds, key=lambda x: (x[2][2]-x[2][0])**2+(x[2][3]-x[2][1])**2)[2]
        last_pred = np.array(biggest)
        return biggest
    else:
        closest = min(preds, key=lambda x: np.linalg.norm(last_pred-np.array(x[2])))[2]
        last_pred = np.array(closest)
        return closest