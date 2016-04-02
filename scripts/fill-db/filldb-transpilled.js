'use strict';

require('babel-register');

var _ = require('lodash');
var Redis = require('redis');

var links = _.map(require('./reading'), function (link) {
  return _.assign(link, {
    tags: link.tags.split(' ')
  });
});

const client = Redis.createClient(6379, '127.0.0.1'); // local

var tags = function tags(links) {
  return _(links).map(function (link) {
    return link.tags;
  }).flatten().countBy().value();
};

var groupedByTags = Object.keys(tags(links)).reduce(function (m, tag) {
  m[tag] = links.map(function (link) {
    return {
      title: link.description,
      url: link.href,
      description: link.extended,
      tags: link.tags
    };
  }).filter(function (link) {
    return link.tags.indexOf(tag) !== -1;
  });
  return m;
}, {});

// add tags:tagname
_.forEach(groupedByTags, function (v, k) {
  v.forEach(function (link) {
    client.sadd('tags:' + k, JSON.stringify(link), function (err, res) {
      console.log('err:', err);
      console.log('res:', res);
    });
  });
});

var hashToZset = function hashToZset(zset, hash) {
  return [zset].concat(_(hash).values().zipWith(_.keys(hash)).flatten().value());
};

// console.log(hashToZset('tags', tags))

// // add tags to redis
client.zadd(hashToZset('tags', tags(links)), function (err, res) {
  console.log('err:', err);
  console.log('res:', res);
});

var jsonArrayToZset = function jsonArrayToZset(key, rawLinks) {
  return [key].concat(_(rawLinks).map(function (link) {
    return [new Date(link.time).getTime(), JSON.stringify({
      title: link.description,
      url: link.href,
      description: link.extended,
      tags: link.tags
    })];
  }).flatten().value());
};

// add links to redis
client.zadd(jsonArrayToZset('links', links), function (err, res) {
  console.log('err:', err);
  console.log('res:', res);
});

// const parseZset = (zset) => _(zset)
//   .chunk(2)
//   .map(kv => ({
//     name: kv[0],
//     count: kv[1]
//   })).value();

// // console.log("hashToZset:", JSON.stringify(hashToZset('tags', tags)));

// // retrieve zset from redis
// // setTimeout(function(){
//   // client.zrevrangebyscore(['links', '+inf', '-inf', 'WITHSCORES'], function(err, res) {
//   //   console.log("err:", err);
//   //   console.log("res:", res);
//   // });
// // }, 5000);

// // const retrieveZset = (key, f) => client.zrevrangebyscore([key, '+inf', '-inf', 'WITHSCORES'], f);
// // const parseTags = (zset) => _(zset)
// //   .chunk(2)
// //   .map(kv => ({
// //     name: kv[0],
// //     count: kv[1]
// //   })).value();

// // const parseLinks = (zset) => _(zset)
// //   .map(JSON.parse)
// //   .chunk(2)
// //   .map(kv => _.assign({}, kv[0], {
// //     createdAt: kv[1]
// //   })).value()
setTimeout(function () {
  process.exit(0);
}, 90000);

