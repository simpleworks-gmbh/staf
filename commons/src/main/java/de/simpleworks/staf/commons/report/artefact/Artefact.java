package de.simpleworks.staf.commons.report.artefact;

import de.simpleworks.staf.commons.enums.ArtefactEnum;

public abstract class Artefact<T> {
	protected ArtefactEnum type = ArtefactEnum.UNKNOWN;

	protected T artefact;

	public abstract T getArtefact();

	public ArtefactEnum getType() {
		return type;
	}
}
