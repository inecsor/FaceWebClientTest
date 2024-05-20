const {faceSdk, readImageBytes, ...paths} = require('./misc/pathsAndUrls');
const chai = require('chai');
const assert = chai.assert;


function validateResponse(response, expectedDetectionsCount, expectedFirst, expectedSecond) {
    assert.strictEqual(response.code, 0, `Unexpected response code: ${response.code}`);
    assert.isArray(response.detections, "'detections' field should be an array");
    assert.lengthOf(response.detections, expectedDetectionsCount, `'detections' array should have ${expectedDetectionsCount} entries`);
    assert.property(response, 'results', "'results' field not found in response");
    assert.isArray(response.results, "'results' should be an array");
    assert.strictEqual(response.results[0].first, expectedFirst, `Expected 'first' to be ${expectedFirst}`);
    if (typeof expectedSecond !== 'undefined') {
        assert.strictEqual(response.results[0].second, expectedSecond, `Expected 'second' to be ${expectedSecond}`);
    }
}

function createMatchRequest(type1, path1, type2 = null, path2 = null) {
    const face1Bytes = readImageBytes(path1);
    const images = [
        {index: 1, data: face1Bytes, type: type1}
    ];

    if (type2 && path2) {
        const face2Bytes = readImageBytes(path2);
        images.push({index: 2, data: face2Bytes, type: type2});
    }

    return {images};
}

describe('Matching Tests', async function () {

    it('test matching API', async function () {
        const matchRequest = createMatchRequest(3, paths.face1Path,
            2, paths.face2Path)
        const response = await faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 2, 3, 2)
    });

    it('test liveAndPrintedDoc type', async function () {
        const matchRequest = createMatchRequest(3, paths.livePhotoPath,
            1, paths.printedDocumentPath)
        const response = await faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 2, 3, 1)
    });

    it('test docWithLive type', async function () {
        const matchRequest = createMatchRequest(4, paths.documentWithLivePath)
        const response = await faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 1, 4)
    });

    it('test external type', async function () {
        const matchRequest = createMatchRequest(5, paths.livePhotoPath,
            3, paths.printedDocumentPath)
        const response = await faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 2, 5, 3)
    });

    it('test document rfid type', async function () {
        const matchRequest = createMatchRequest(2, paths.livePhotoPath,
            1, paths.printedDocumentPath)
        const response = await faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 2, 2, 1)
    });

    it('test ghost type', async function () {
        const matchRequest = createMatchRequest(6, paths.livePhotoPath,
            1, paths.printedDocumentPath)
        const response = await faceSdk.matchingApi.match(matchRequest);
        validateResponse(response, 2, 6, 1)
    });
});
