# stream and definitions variables should be injected
# we do nothing and return the stream
# what would really be needed is to either process the stream in jython (which may be hard)
# or to return a jython implementation of the necessary java interface needed in stream
# processing. A good description of how this could be done can be found here:
# https://wiki.python.org/jython/JythonMonthly/Articles/September2006/1
# but it might also be possible to just pass back a jython function in some cases
# (equivalent to a java lambda expression).

stream

