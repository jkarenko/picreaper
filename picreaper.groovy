import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Rectangle
import java.awt.Graphics
import java.awt.image.PixelGrabber
import java.awt.image.ImageObserver

ratio = 16/9
width = 160
height = (int)width/ratio

for (a in this.args) {
	handleImage(a)
}

def resize(img) {
	BufferedImage sourceImage = img;
	Image thumbnail = sourceImage.getScaledInstance(width, -1, Image.SCALE_SMOOTH);
	BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null),
		thumbnail.getHeight(null),
		BufferedImage.TYPE_INT_RGB);
	bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);
	return bufferedThumbnail
}


def cropImage(src, yPos, stepSize) {
	BufferedImage dest = src.getSubimage(0, yPos, src.getWidth(), stepSize);
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
	filename = file
	image = new File(filename)
	outputStream = new File("${width}_$filename")
	imgStream = ImageIO.read(image)
	
	stepSize = 20


	// resize image
	newImage = resize(imgStream)


	// entropy comparison and cropping
	while(newImage.getHeight() > height) {
		sliceFromTop = handlepixels(newImage, 0, 0, newImage.getWidth(), stepSize)
		sliceFromBottom = handlepixels(newImage, 0, (newImage.getHeight() - stepSize), newImage.getWidth(), stepSize)

		if (stepSize > newImage.getHeight() - height) {
			stepSize = newImage.getHeight() - height
		}

		if (sliceFromTop >= sliceFromBottom) {
			msg = "slice from bottom"
			newImage = cropImage(newImage, 0, (newImage.getHeight() - stepSize ))
		}
		else {
			msg = "slice from top"
			newImage = cropImage(newImage, stepSize, newImage.getHeight() - stepSize)
		}

		println filename + ": " + msg + ", " + newImage.getWidth() + " x " + newImage.getHeight() + ", (want " + width + " x " + height + ")"
	}

	// finally save the resized and cropped image
	saveImage(newImage)
}