'use strict';

require('babel-register');

const _ = require('lodash');
const Redis = require('redis');

const links = _.map(require('./reading'), link => _.assign(link, {
  tags: link.tags.split(' ')
}));

const client = Redis.createClient(6379, '127.0.0.1'); // local

const tags = (links) => _(links)
  .map(link => link.tags)
  .flatten()
  .countBy()
  .value();

const groupedByTags = Object.keys(tags(links)).reduce((m, tag) => {
  m[tag] = links
  .map(link => ({
    title: link.description,
    url: link.href,
    description: link.extended,
    tags: link.tags
  })).filter(function(link) {
    return link.tags.indexOf(tag) !== -1;
  });
  return m;
}, {});

// add tags:tagname
_.forEach(groupedByTags, (v, k) => {
  v.forEach(link => {
    client.sadd(`tags:${k}`, JSON.stringify(link), function(err, res) {
      console.log("err:", err);
      console.log("res:", res);
    });
  });
});

const hashToZset = (zset, hash) => [zset].concat(_(hash)
  .values()
  .zipWith(_.keys(hash))
  .flatten()
  .value());

// console.log(hashToZset('tags', tags))

// // add tags to redis
client.zadd(hashToZset('tags', tags(links)), function(err, res){
  console.log("err:", err);
  console.log("res:", res);
});

const jsonArrayToZset = (key, rawLinks) => [key].concat(
  _(rawLinks).map(link => [new Date(link.time).getTime(), JSON.stringify({
    title: link.description,
    url: link.href,
    description: link.extended,
    tags: link.tags
  })]).flatten().value()
);

// add links to redis
client.zadd(jsonArrayToZset('links', links), function(err, res){
  console.log("err:", err);
  console.log("res:", res);
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
// optional
setTimeout(function() {
  process.exit(0);
}, 15000);
