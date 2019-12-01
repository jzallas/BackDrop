require('../index');
const validator = require('validator');

const timeout = 30000 // 30 seconds in millis

describe('getVideoInfo', () => {

    const testUrl = "https://www.youtube.com/watch?v=_HSylqgVYQI"
    const testId = "123"

    test('url is available for all formats', done => {
        android.onFailure = jest.fn((id, result) => {
            done.fail()
        })
        android.onSuccess = jest.fn((id, result) => {
            expect(id).toBe(testId)
            expect(result).toBeDefined()

            let info = JSON.parse(result)

            info.formats.forEach(format => {
                expect(format.url).toBeDefined()
                expect(validator.isURL(format.url)).toBe(true)
            })
            done()
        })

        window.app.getVideoInfo(testId, testUrl)
    }, timeout)
})

