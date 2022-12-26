
import ctrmap.Launc;
import ctrmap.editor.CTRMapMenuActions;
import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.editor.gui.workspace.ROMExportDialog;
import ctrmap.editor.system.juliet.CTRMapPluginInterface;
import ctrmap.editor.system.juliet.ICTRMapPlugin;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.formats.common.GameInfo;
import ctrmap.formats.ntr.rom.srl.NDSROM;
import ctrmap.util.tools.cont.ContainerUtil;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.KeyStroke;
import rtldr.JRTLDRCore;
import xstandard.fs.FSFile;
import xstandard.fs.VFSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.fs.accessors.MemoryFile;
import xstandard.fs.accessors.ProxyFile;
import xstandard.fs.accessors.arc.ArcFile;
import xstandard.fs.accessors.arc.ArcInput;
import xstandard.gui.DialogUtils;

public class CTRMapPlugin implements ICTRMapPlugin {

	public CTRMapPlugin() {}

	public static void main(String[] args) {
		JRTLDRCore.suppressDebugPluginByFileName("Sparkplug.jar");
		JRTLDRCore.addDebugSelfClassLoader(CTRMapPlugin.class.getProtectionDomain().getCodeSource());
		Launc.main(null);
	}

	@Override
	public void registPerspectives(CTRMapPluginInterface j) {
	}

	@Override
	public void registEditors(CTRMapPluginInterface j) {
//		j.rmoRegistToolbarEditors(...);
//		j.rmoRegistTabbedEditors(...);
	}

	private void loadVFS(MemoryFile destDir, VFSFile src) {
		for (VFSFile child : src.listFiles()) {
			FSFile ov = child.getOvFile();
			FSFile base = child.getBaseFile();
			if (base == null || !base.exists()) {
				destDir.linkChild(new ProxyFile(ov, ov.getPathRelativeTo(src.getVFS().getOvFSRoot()))); //use entire overlay file directly
			} else {
				//merge ovfs into basefs
				if (!ov.exists()) {
					destDir.linkChild(new ProxyFile(base, base.getPathRelativeTo(src.getVFS().getBaseFSRoot())));
				} else {
					if (base instanceof ArcFile) {
						ArcFile arc = (ArcFile) base;
						ArcInput[] inputs = src.getVFS().getArcInputs(ov, ov).toArray(new ArcInput[0]);
						if (inputs.length > 0) {
							MemoryFile newArc = new MemoryFile(arc.getName(), arc.getBytes());
							ArcFile newArcFileObj = new ArcFile(newArc, src.getVFS().getArcFileAccessor());
							src.getVFS().getArcFileAccessor().writeToArcFile(newArcFileObj, null, inputs);
							arc = newArcFileObj;
						}
						destDir.linkChild(new ProxyFile(arc.getSource(), base.getPathRelativeTo(src.getVFS().getBaseFSRoot())));
					} else if (base.isDirectory()) {
						MemoryFile subDir = destDir.createChildDir(base.getName());
						loadVFS(subDir, child);
					} else {
						destDir.linkChild(new ProxyFile(ov, ov.getPathRelativeTo(src.getVFS().getOvFSRoot())));
					}
				}
			}
		}
	}

	@Override
	public void registUI(CTRMapPluginInterface j, GameInfo game) {
		if (game.isGenV()) {
			j.rmoAddAboutDialogCredits(
				"Test"
			);
			j.rmoAddAboutDialogSpecialThanks(
				"Test"
			);
		}
	}
}
