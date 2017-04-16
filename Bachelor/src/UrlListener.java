import google.RedirectUrlListener;

public class UrlListener implements RedirectUrlListener{

	@Override
	public void notifyUrl(String url) {
		System.out.println("Url Received: " + url);
		
	}
	

}
