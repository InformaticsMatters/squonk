/**
 *
 * @author timbo
 */
class MySplitter {
    
    static Iterator split(String s) {
        String[] vals = s.split(",")
        List list = Arrays.asList(vals)
        return list.iterator()
    }
	
}

