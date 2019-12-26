const { getVideoInfo } = require('../index');
const validator = require('validator');

const timeout = 30000 // 30 seconds in millis

describe('getVideoInfo', () => {

    const testUrl = "https://www.youtube.com/watch?v=_HSylqgVYQI"

    test('url is available for all formats', () => {

        expect.hasAssertions()

        return getVideoInfo(testUrl).then(result => {
                expect(result).toBeDefined()

                result.formats.forEach(format => {
                    expect(format.url).toBeDefined()
                    expect(validator.isURL(format.url)).toBe(true)
                })
            })

    }, timeout)
})
