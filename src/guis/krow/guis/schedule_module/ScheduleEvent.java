package krow.guis.schedule_module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import zeale.guis.schedule_module.ScheduleModule;

public class ScheduleEvent implements Serializable, Comparable<ScheduleEvent> {

	public final SimpleStringProperty description = new SimpleStringProperty(), name = new SimpleStringProperty();
	public final SimpleLongProperty dueDate = new SimpleLongProperty();
	private transient File file = new File(ScheduleModule.DATA_DIR, UUID.randomUUID().toString());

	public boolean autoSave = true;

	private HashMap<DataKey, Object> serializationData;

	private final ChangeListener<Object> onChanged = (observable, oldValue, newValue) -> {
		if (autoSave)
			save();
	};

	{
		description.addListener(onChanged);
		name.addListener(onChanged);
		dueDate.addListener(onChanged);
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public ScheduleEvent() {
		this(new Date().getTime() + TimeUnit.DAYS.toMillis(1));
	}

	public ScheduleEvent(String description, String name) {
		this(description, name, new Date().getTime() + TimeUnit.DAYS.toMillis(1));
	}

	public ScheduleEvent(String description, String name, long dueDate) {
		this.description.set(description);
		name = name == null ? "Unnamed" : name;
		this.name.set(name);
		this.dueDate.set(dueDate);
	}

	public ScheduleEvent(long dueDate) {
		this(null, null);
		this.dueDate.set(dueDate);
	}

	private void writeObject(ObjectOutputStream os) throws IOException {
		HashMap<DataKey, Object> data = new HashMap<>(3);

		data.put(DataKey.DESCRIPTION, description.get());
		data.put(DataKey.NAME, name.get());
		data.put(DataKey.DUE_DATE, dueDate.get());

		os.writeObject(data);
	}

	private Object readResolve() throws ObjectStreamException {
		return new ScheduleEvent(this);
	}

	private ScheduleEvent(ScheduleEvent copy) {
		autoSave = copy.autoSave;
		file = copy.file;
		description.set((String) copy.serializationData.get(DataKey.DESCRIPTION));
		name.set((String) copy.serializationData.get(DataKey.NAME));
		dueDate.set((long) copy.serializationData.get(DataKey.DUE_DATE));
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream is) throws IOException {
		try {
			serializationData = (HashMap<DataKey, Object>) is.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void save() {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		} else if (file.exists())
			file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
			oos.writeObject(this);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private enum DataKey {
		DESCRIPTION, NAME, DUE_DATE;
	}

	public long getTimeUntilDue() throws IllegalArgumentException {
		return dueDate.get() - System.currentTimeMillis();
	}

	@Override
	public int compareTo(ScheduleEvent o) {
		if (o.dueDate.get() > dueDate.get())
			return -1;
		else if (o.dueDate.get() < dueDate.get())
			return 1;
		else
			return 0;
	}

	public static ScheduleEvent load(File file) throws FileNotFoundException, IOException {
		try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(file))) {
			ScheduleEvent sc = (ScheduleEvent) is.readObject();
			sc.file = file;
			return sc;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
