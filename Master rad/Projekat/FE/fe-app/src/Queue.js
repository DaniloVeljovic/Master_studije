export default function Queue(initialValue = [{},{},{},{},{}]) {
    // initialise the queue and offset
    var queue = initialValue
    var cnt = 0
    var maxSize = 5
  
    // Returns the the queue.
    this.get = function() {
      return queue
    }
  
    // Returns the length of the queue.
    // this.getLength = function() {
    //   return (queue.length - offset)
    // }
  
    // Returns true if the queue is empty, and false otherwise.
    // this.isEmpty = function() {
    //   return (queue.length === 0)
    // }
  
    /* Enqueues the specified item. The parameter is:
     *
     * item - the item to enqueue
     */
    this.enqueue = function(item) {
      queue[5] = queue[4]
      queue[4] = queue[3]
      queue[3] = queue[2]
      queue[2] = queue[1]
      queue[1] = queue[0]
      queue[0] = item
    }
  
    /* Dequeues an item and returns it. If the queue is empty, the value
     * 'undefined' is returned.
     */
    // this.dequeue = function() {
    //   // if the queue is empty, return immediately
    //   if (!queue.length) return undefined
  
    //   // store the item at the front of the queue
    //   var item = queue[offset]
  
    //   // increment the offset and remove the free space if necessary
    //   if (++offset * 2 >= queue.length) {
    //     queue = queue.slice(offset)
    //     offset = 0
    //   }
  
    //   // return the dequeued item
    //   return item
    // }
  
    /* Returns the item at the front of the queue (without dequeuing it). If the
     * queue is empty then undefined is returned.
     */
    // this.peek = function() {
    //   return (queue.length > 0 ? queue[offset] : undefined)
    // }
  }