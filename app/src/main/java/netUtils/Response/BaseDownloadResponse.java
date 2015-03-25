package netUtils.response;

import java.io.InputStream;

public class BaseDownloadResponse
{
	private InputStream inputStream;

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
