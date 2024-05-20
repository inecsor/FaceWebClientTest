const path = require('path');
const {FaceSdk} = require("@regulaforensics/facesdk-webclient");
const fs = require("fs");

const files_path = path.join(__dirname, 'files');

// Use an environment variable for apiBasePath, with a default fallback
const apiBasePath = process.env.ServiceLink || "http://localhost:41101/";

const faceSdk = new FaceSdk({
    basePath: apiBasePath
});

function readImageBytes(imagePath) {
    return fs.readFileSync(imagePath);
}

module.exports = {
    face1Path: path.join(files_path, "face1.jpg"),
    face2Path: path.join(files_path, "face2.jpg"),
    face3Path: path.join(files_path, "face3.jpg"),
    livePhotoPath: path.join(files_path, "me.png"),
    printedDocumentPath: path.join(files_path, "printedDoc.png"),
    documentWithLivePath: path.join(files_path, "me_and_id.png"),
    severalFacesImagePath: path.join(files_path, "severalFaces.jpg"),
    faceSdk,
    readImageBytes
};
