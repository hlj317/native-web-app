/**
 * Imports
 */

var forEach = require('..')
var test = require('tape')

/**
 * Tests
 */

 test('should work', function (t) {
   t.plan(2)

   forEach(function (val, key) {
     t.equal(val, 2)
     t.equal(key, 'a')
   }, {a: 2})
 })
