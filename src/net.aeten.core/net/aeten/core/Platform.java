package net.aeten.core;

public class Platform {
	public enum OperatingSystem {
		MAC,
		LINUX,
		WINDOWS,
		SOLARIS,
		FREEBSD,
		OPENBSD,
		WINDOWSCE,
		AIX,

		UNKNOWN;

		private final String name = super.toString().toLowerCase();

		@Override
		public String toString() {
			return this == UNKNOWN? OS_NAME: name;
		}
	}

	public enum Arch {
		X86,
		X86_64,
		PPC,
		PPC_64,

		UNKNOWN;

		private final String name = super.toString().toLowerCase();

		@Override
		public String toString() {
			return this == UNKNOWN? ARCH_NAME: name;
		}
	}

	public final static OperatingSystem OS;
	public final static Arch ARCH;

	public final static String OS_NAME = System.getProperty("os.name");
	public final static String ARCH_NAME = System.getProperty("os.arch").toLowerCase();

	static {
		if (OS_NAME.startsWith("Linux")) {
			OS = OperatingSystem.LINUX;
		} else if (OS_NAME.startsWith("FreeBSD")) {
			OS = OperatingSystem.FREEBSD;
		} else if (OS_NAME.startsWith("OpenBSD")) {
			OS = OperatingSystem.OPENBSD;
		} else if (OS_NAME.startsWith("Mac") || OS_NAME.startsWith("Darwin")) {
			OS = OperatingSystem.MAC;
		} else if (OS_NAME.startsWith("Solaris") || OS_NAME.startsWith("SunOS")) {
			OS = OperatingSystem.SOLARIS;
		} else if (OS_NAME.startsWith("Windows")) {
			OS = OperatingSystem.WINDOWS;
		} else if (OS_NAME.startsWith("Windows CE")) {
			OS = OperatingSystem.WINDOWSCE;
		} else if (OS_NAME.startsWith("AIX")) {
			OS = OperatingSystem.AIX;
		} else {
			OS = OperatingSystem.UNKNOWN;
		}

		switch (ARCH_NAME) {
		case "i386":
		case "i486":
		case "i586":
		case "i686":
		case "x86":
			ARCH = Arch.X86;
			break;
		case "x86_64":
		case "amd64":
			ARCH = Arch.X86_64;
			break;
		case "powerpc":
			ARCH = Arch.PPC;
			break;
		case "powerpc64":
			ARCH = Arch.PPC_64;
			break;
		default:
			ARCH = Arch.UNKNOWN;
			break;
		}
	}

	public static final boolean isLinux() {
		return OS == OperatingSystem.LINUX;
	}

	public static final boolean isFreeBSD() {
		return OS == OperatingSystem.FREEBSD;
	}

	public static final boolean isOpenBSD() {
		return OS == OperatingSystem.OPENBSD;
	}

	public static final boolean isSolaris() {
		return OS == OperatingSystem.SOLARIS;
	}

	public static final boolean isMac() {
		return OS == OperatingSystem.MAC;
	}

	public static final boolean isAix() {
		return OS == OperatingSystem.AIX;
	}

	/** Returns true for any windows variant. */
	public static final boolean isWindows() {
		return OS == OperatingSystem.WINDOWS || OS == OperatingSystem.WINDOWSCE;
	}

	public static final boolean isWindowsCE() {
		return OS == OperatingSystem.WINDOWSCE;
	}

}
