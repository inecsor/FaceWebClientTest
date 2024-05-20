const {FaceQualityScenarios} = require("@regulaforensics/facesdk-webclient");
const {faceSdk, readImageBytes, ...paths} = require('./misc/pathsAndUrls');
const chai = require('chai');
const assert = chai.assert;


let oneFace, sevFaces;

function basicAssertions(response) {
    assert.strictEqual(response.code, 0, `Unexpected response code: ${response.code}`);
    assert.property(response, 'results', "'results' field not found in response");
    assert.isArray(response.results.detections, "'detections' should be an array and not be empty");
    assert.isNotEmpty(response.results.detections, "'detections' array is empty");
}

describe('Detect Tests', function () {

    beforeEach(function () {
        oneFace = (readImageBytes(paths.face1Path)).toString('base64');
        sevFaces = (readImageBytes(paths.severalFacesImagePath)).toString('base64');
    });

    it('should test detect API', async function () {
        const request = {image: oneFace};
        const response = await faceSdk.matchingApi.detect(request);
        basicAssertions(response)
    });

    it('should test QualityFull scenario', async function () {
        let scenario = FaceQualityScenarios.QUALITY_FULL
        const params = {
            scenario: scenario,
        };
        const request = {
            image: oneFace,
            processParam: params
        };
        const response = await faceSdk.matchingApi.detect(request);
        basicAssertions(response)
        assert.strictEqual(response.results.scenario, scenario,
            `Expected scenario to be ${scenario} but got ${response.results.scenario}`);
    });

    it('should test cropAllFaces scenario', async function () {
        const params = {
            scenario: FaceQualityScenarios.CROP_ALL_FACES,
            onlyCentralFace: false,
        };
        const request = {image: sevFaces, processParam: params};
        const response = await faceSdk.matchingApi.detect(request);

        basicAssertions(response)
        assert.lengthOf(response.results.detections, 5,
            `Expected 5 detections, but got ${response.results.detections.length} detections`);
    });

    it('should test only central face', async function () {
        const params = {
            onlyCentralFace: true,
        };
        const request = {image: sevFaces, processParam: params};
        const response = await faceSdk.matchingApi.detect(request);

        basicAssertions(response)
        assert.lengthOf(response.results.detections, 1,
            `Expected 1 detection, but got ${response.results.detections.length} detections`);
    });

    it('should test outputImageParams', async function () {
        const params = {
            outputImageParams: {
                backgroundColor: [128, 128, 128],
                crop: {type: 0, padColor: [0, 0, 0], size: [300, 400]},
            },
        };
        const request = {image: sevFaces, processParam: params};
        const response = await faceSdk.matchingApi.detect(request);
        basicAssertions(response)
        assert.isNotEmpty(response.results.detections[0].crop, "'crop' field is empty");
    });

    it('should test quality', async function () {
        const params = {
            quality: {
                backgroundMatchColor: [128, 128, 128],
                config: [{name: "Roll", range: [-5, 5]}]
            },
        };
        const request = {image: sevFaces, processParam: params};
        const response = await faceSdk.matchingApi.detect(request);
        basicAssertions(response)
        assert.strictEqual(response.results.detections[0].quality.details[0].name, 'Roll',
            "Name of the quality detail does not match");
        assert.deepEqual(response.results.detections[0].quality.details[0].range, [-5, 5],
            "Range of the quality detail does not match");
    });

    it('should test attributes', async function () {
        const params = {
            attributes: {
                config: [{name: 'Age', range: [5, 45]}]
            }
        };
        const request = {image: sevFaces, processParam: params};
        const response = await faceSdk.matchingApi.detect(request);
        basicAssertions(response)
        assert.strictEqual(response.results.detections[0].attributes.details[0].name, 'Age',
            "Name of the attribute detail does not match");
        assert.isAtLeast(response.results.detections[0].attributes.details[0].value[0], 5,
            "First value of the attribute detail is not within range");
        assert.isAtMost(response.results.detections[0].attributes.details[0].value[0], 45,
            "First value of the attribute detail is not within range");
        assert.isAtLeast(response.results.detections[0].attributes.details[0].value[1], 5,
            "Second value of the attribute detail is not within range");
        assert.isAtMost(response.results.detections[0].attributes.details[0].value[1], 45,
            "Second value of the attribute detail is not within range");
    });
});

