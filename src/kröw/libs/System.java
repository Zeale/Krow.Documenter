package kr�w.libs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.Date;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import kr�w.zeale.v1.program.core.DataManager;

public class System extends MindsetObject {

	/**
	 * The description of this {@link System}.
	 */
	private transient StringProperty description;

	/**
	 * The date that this {@link System} was made.
	 */
	private transient Date creationDate;

	/**
	 * Constructs a new {@link System} using a name, description, and date of
	 * creation.
	 *
	 * @param name
	 *            The name of this {@link System}.
	 * @param description
	 *            A description of this {@link System}.
	 * @param creationDate
	 *            The date this {@link System} was made.
	 */
	public System(final String name, final String description, final Date creationDate) {
		super(name);
		this.name = new SimpleStringProperty(name);
		this.description = new SimpleStringProperty(description);
		this.creationDate = creationDate;
	}

	/**
	 * The serialVersionUID of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The version of this class. This is used for backwards compatibility. See
	 * {@link #readObject(ObjectInputStream)} for more details.
	 */
	private static final long trueSerialVersionUID = 1L;

	/**
	 * The file extension of this {@link MindsetObject}. This is used when
	 * saving {@link System} objects to the file system. This {@link String} is
	 * the file extension of the file that this {@link System} will be saved as.
	 */
	public static final String FILE_EXTENSION = ".syst";

	/**
	 * The <code>readObject</code> method of this class.
	 *
	 * @param is
	 *            The {@link ObjectStreamException} passed in by Serialization.
	 * @throws IOException
	 *             If an {@link IOException} ocurrs. Also necessary to match the
	 *             method signature of the {@code readObject()} method used in
	 *             Serialization.
	 */
	private void readObject(final ObjectInputStream is) throws IOException {
		try {
			final long version = is.readLong();
			if (version == System.trueSerialVersionUID) {

				name = new SimpleStringProperty((String) is.readObject());
				description = new SimpleStringProperty((String) is.readObject());
				creationDate = (Date) is.readObject();
			}
		} catch (final ClassNotFoundException e) {
			// TODO: handle exception
		}
	}

	/**
	 * The <code>writeObject</code> method of this class.
	 *
	 * @param os
	 *            The {@link ObjectOutputStream} passed in by Serialization.
	 * @throws IOException
	 *             If an {@link IOException} ocurrs. Also necessary to match the
	 *             method signature of the {@code writeObject} method called by
	 *             Serialization.
	 */
	private void writeObject(final ObjectOutputStream os) throws IOException {
		os.writeLong(System.trueSerialVersionUID);
		os.writeObject(name.get());
		os.writeObject(description.get());
		os.writeObject(creationDate);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see kr�w.libs.MindsetObject#getExtension()
	 */
	@Override
	String getExtension() {
		return System.FILE_EXTENSION;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see kr�w.libs.MindsetObject#getSaveDirectory()
	 */
	@Override
	File getSaveDirectory() {
		return DataManager.SYSTEM_SAVE_DIRECTORY;
	}

	/**
	 * Getter for the {@link #description} property of this {@link System}.
	 *
	 * @return The {@link #description} property for this {@link System}.
	 */
	public StringProperty descriptionProperty() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof System && ((System) obj).getName().equals(getName());
	}

	/**
	 * A getter for the {@link #creationDate} of this {@link System}. This is
	 * the noted date that this {@link System} was created.
	 *
	 * @return A {@link Date} object representing the date that this
	 *         {@link System} was made.
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * A getter for the description of this {@link System} as a {@link String}.
	 *
	 * @return This {@link System}'s description as a {@link String}.
	 */
	public String getDescription() {
		return description.get();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see kr�w.libs.MindsetObject#getFile()
	 */
	@Override
	public File getFile() {
		return new File(getSaveDirectory(), getName() + getExtension());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see kr�w.libs.MindsetObject#getProperty(java.lang.String)
	 */
	@Override
	public ObservableValue<?> getProperty(final String key) {
		if (key.equalsIgnoreCase("Name"))
			return new ReadOnlyObjectWrapper<>(name.get());
		else if (key.equalsIgnoreCase("Description"))
			return new ReadOnlyObjectWrapper<>(description.get());
		else if (key.equalsIgnoreCase("CreationDate") || key.equalsIgnoreCase("Creation Date")
				|| key.equalsIgnoreCase("Creation-Date"))
			return new ReadOnlyObjectWrapper<>(creationDate);
		return null;
	}

	/**
	 * Sets this {@link System}'s date of creation given a {@link Date} object.
	 *
	 * @param creationDate
	 *            The new date of creation of this {@link System}.
	 */
	public void setCreationDate(final Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Sets this {@link System}'s description given a {@link String}.
	 *
	 * @param description
	 *            The new description of this {@link System}.
	 */
	public void setDescription(final String description) {
		this.description.set(description);
	}

}
