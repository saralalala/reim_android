package netUtils.Response;

import java.io.InputStream;

public class BaseDownloadResponse
{
	private InputStream inputStream = null;

	public BaseDownloadResponse(Object httpResponse)
	{
		inputStream = (InputStream)httpResponse;
	}

	public InputStream getInputStream()
	{
		return inputStream;
	}

	public void setInputStream(InputStream inputStream)
	{
		this.inputStream = inputStream;
	}

}
