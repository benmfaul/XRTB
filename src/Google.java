import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidRequest.Imp.Video;
import com.google.openrtb.OpenRtb.Protocol;
import com.google.openrtb.OpenRtb.VideoLinearity;

public class Google {
	
	public static void main(String [] args) {
		new Google();
	}
	public Google() {

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
