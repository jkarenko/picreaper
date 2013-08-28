import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Rectangle
import java.awt.Graphics
import java.awt.image.PixelGrabber
import java.awt.image.ImageObserver

for (a in this.args) {
	handleImage(a)
}

def resize(img, resizeWidth, resizeHeight) {
	BufferedImage sourceImage = img;
	Image thumbnail = sourceImage.getScaledInstance(resizeWidth, resizeHeight, Image.SCALE_SMOOTH);
	BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null),
		thumbnail.getHeight(null),
		BufferedImage.TYPE_INT_RGB);
	bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);
	return bufferedThumbnail
}


def cropImage(src, xPos, yPos, xAmount, yAmount) {
	BufferedImage dest = src.getSubimage(xPos, yPos, xAmount, yAmount);
	return dest; 
}


def handlepixels(Image img, int x, int y, int w, int h) {
	pixels = new int[w * h];
	pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
	try {
		pg.grabPixels();
	} 
	catch (InterruptedException e) {
		System.err.println("interrupted waiting for pixels!");
		return;
	}
	if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
		System.err.println("image fetch aborted or errored");
		return;
	}
	pixelList = []
	for (int j = 0; j < h; j++) {
		for (int i = 0; i < w; i++) {
			pixelList.add(pixels[j * w + i])
		}
	}
	return calculateEntropy(pixelList)
}

def calculateEntropy(List<String> values) {
	map = new HashMap<String, Integer>();
	// count the occurrences of each value
	for (sequence in values) {
		if (!map.containsKey(sequence)) {
			map.put(sequence, 0);
		}
		map.put(sequence, map.get(sequence) + 1);
	}

	// calculate the entropy
	Double result = 0.0;
	for (sequence in map.keySet()) {
		Double frequency = (double) map.get(sequence) / values.size();
		result -= frequency * (Math.log(frequency) / Math.log(2));
	}

	return result;
}


def saveImage(img) {
	ImageIO.write(img, "jpeg", outputStream);
}



def handleImage(file) {
	ratio = 16/9
	width = 160
	height = (int)width/ratio

	filename = file
	image = new File(filename)
	outputStream = new File("${width}_$filename")
	imgStream = ImageIO.read(image)
	
	origHeight = imgStream.getHeight()
	origWidth = imgStream.getWidth()

	// decide whether to preserve height or width
	if ((origWidth / origHeight) > ratio) {
		resizeHeight = height
		resizeWidth = -1
	} 
	else {
		resizeHeight = -1
		resizeWidth = width
	}

	stepSize = 20


	// resize image now to speed up the rest of the process
	newImage = resize(imgStream, resizeWidth, resizeHeight)


	// entropy comparison and cropping for tall images
	while(newImage.getHeight() > height) {
		// copy the pixels of a $stepSize wide slice of the top and bottom of image and calculate entropy for both
		sliceFromTop = handlepixels(newImage, 0, 0, newImage.getWidth(), stepSize)
		sliceFromBottom = handlepixels(newImage, 0, (newImage.getHeight() - stepSize), newImage.getWidth(), stepSize)

		// make sure we're not cropping too much out of the image
		if (stepSize > newImage.getHeight() - height) {
			stepSize = newImage.getHeight() - height
		}

		// see which sample slice has less entropy and throw that one away
		if (sliceFromTop >= sliceFromBottom) {
			msg = "slice from bottom"
			newImage = cropImage(newImage, 0, 0, newImage.getWidth(), (newImage.getHeight() - stepSize ))
		}
		else {
			msg = "slice from top"
			newImage = cropImage(newImage, 0, stepSize, newImage.getWidth(), newImage.getHeight() - stepSize)
		}

		println filename + ": " + msg + ", " + newImage.getWidth() + " x " + newImage.getHeight() + ", (want " + width + " x " + height + ")"
	}

	// entropy comparison and cropping for wide images
	while(newImage.getWidth() > width) {
		sliceFromLeft = handlepixels(newImage, 0, 0, stepSize, newImage.getHeight())
		sliceFromRight = handlepixels(newImage, (newImage.getWidth() - stepSize), 0, stepSize, newImage.getHeight())

		if (stepSize > newImage.getWidth() - width) {
			stepSize = newImage.getWidth() - width
		}

		if (sliceFromRight >= sliceFromLeft) {
			msg = "slice from left"
			newImage = cropImage(newImage, stepSize, 0, (newImage.getWidth() - stepSize), newImage.getHeight())
		}
		else {
			msg = "slice from right"
			newImage = cropImage(newImage, 0, 0, (newImage.getWidth() - stepSize), newImage.getHeight())
		}
		
		println filename + ": " + msg + ", " + newImage.getWidth() + " x " + newImage.getHeight() + ", (want " + width + " x " + height + ")"
	}

	// save the resized and cropped image
	saveImage(newImage)
}