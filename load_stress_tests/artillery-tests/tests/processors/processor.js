module.exports = {
    setVertexEventId: setVertexEventId,
    setEdgeEventId: setEdgeEventId,
    setEdgeEventIdLoop: setEdgeEventIdLoop,
    hasNextEvent: hasNextEvent,
};

let eventTime = 10000;

let vertexEventId = 0;
function setVertexEventId(context, events, done) {
    context.vars["vertexEventId"] = vertexEventId + "_" + eventTime;
    vertexEventId += 1;
    eventTime += 1;
    return done();
}

let inVertex = 0;
let outVertex = 5;
function setEdgeEventId(context, events, done){
    context.vars["edgeEventId"] = outVertex + "|label|" + inVertex + "_" + eventTime;
    inVertex += 1;
    outVertex += 1;
    eventTime += 1;
    return done();
}

let maxNeighbors = 1000;
let currNeighborId = 1;
function hasNextEvent(context, next) {
    const continueLooping = currNeighborId <= maxNeighbors;
    // While `continueLooping` is true, the `next` function will
    // continue the loop in the test scenario.
    return next(continueLooping);
}

function setEdgeEventIdLoop(context, events, done){
    context.vars["edgeEventId"] = vertexEventId + "|label|" + currNeighborId + "_" + eventTime;
    currNeighborId += 1;
    eventTime += 1;
    return done();
}
