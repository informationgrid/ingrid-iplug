package de.ingrid.iplug.metadata;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Metadata implements Serializable {

	private static final long serialVersionUID = -882806556761084500L;

	private MetadataAnnotation _metaDataAnnotation;

	private DateFormat _format = new SimpleDateFormat("yyyy-MM-dd");
	
	private Date _releaseDate = new Date(0);
	
	public Metadata() {
		Package package1 = Metadata.class.getPackage();
		_metaDataAnnotation = package1.getAnnotation(MetadataAnnotation.class);
	}

	public String getVersion() {
		return _metaDataAnnotation != null ? _metaDataAnnotation.version()
				: "unknown";
	}

	public Date getReleaseDate() {
		Date date = _releaseDate;
		try {
			date = _metaDataAnnotation != null ? _format
					.parse(_metaDataAnnotation.date()) : _releaseDate;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public IPlugType getIPlugType() {
		return _metaDataAnnotation != null ? _metaDataAnnotation.type()
				: IPlugType.OTHER;
	}
	
	
	public static void main(String[] args) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(format.parse("2007-01-01"));
	}
}
