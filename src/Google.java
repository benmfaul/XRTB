import javax.xml.bind.DatatypeConverter;


import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Video;
import com.google.openrtb.OpenRtb.BidResponse;
import com.google.openrtb.OpenRtb.Protocol;
import com.google.openrtb.OpenRtb.VideoLinearity;

public class Google {
	
	public static void main(String [] args) throws Exception {
		// new Google().request();
		new Google().response("CgMxMjMSoQIKkwIKAzEyMxIBMRl7FK5H4XqEPyIKYmVuOnBheWRheSpYaHR0cDovL2xvY2FsaG9zdDo4MDgwL3J0Yi93aW4vZ29vZ2xlLyR7QVVDVElPTl9QUklDRX0vMC4wLzAuMC9iZW46cGF5ZGF5L3N0cm9lci10ZXN0LzEyMzIjaHR0cDovL2xvY2FsaG9zdDo4MDgwL2ZvcndhcmQ/OTkyMDE6Dm9yaWdpbmF0b3IuY29tQkVodHRwOi8vbG9jYWxob3N0OjgwODAvaW1hZ2VzLzMyMHg1MC5qcGc/YWRpZD1iZW46cGF5ZGF5JiMzODtiaWRpZD0xMjNKCmJlbjpwYXlkYXlSC3N0cm9lci10ZXN0gAHAAogBMhIJZ29vZ2xlLWlkGgMxMjMiA1VTRA==");
	}
	public Google() {

	}
	
	public void response(String proto) throws Exception  {
		byte[] data = DatatypeConverter.parseBase64Binary(proto);
		BidResponse r = BidResponse.parseFrom(data);
		System.out.println(r.toString());
	}

	public void request() {
		BidRequest request = BidRequest.newBuilder()
		    .setId("1")
		    .addBcat("IAB1")
		    .addImp(Imp.newBuilder()
		        .setId("1")
		        .setBidfloor(4000)
		        .setVideo(Video.newBuilder()
		            .setLinearity(VideoLinearity.LINEAR)
		            .addProtocols(Protocol.VAST_3_0)
		            .setW(640)
		            .setH(480)))
		    .build();
	}
}
