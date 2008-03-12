package com.jetbrains.python.psi.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class ResolveImportUtil {
  private ResolveImportUtil() {
  }

  @Nullable
  static PsiElement resolveImportReference(final PyReferenceExpression importRef) {
    final String referencedName = importRef.getReferencedName();
    if (referencedName == null) return null;

    final PyExpression qualifier = importRef.getQualifier();
    if (qualifier instanceof PyReferenceExpression) {
      PsiElement qualifierElement = ((PyReferenceExpression) qualifier).resolve();
      if (qualifierElement == null) return null;
      return resolveChild(qualifierElement, referencedName, importRef);
    }

    if (importRef.getParent() instanceof PyImportElement) {
      PyImportElement parent = (PyImportElement) importRef.getParent();
      if (parent.getParent() instanceof PyFromImportStatement) {
        PyFromImportStatement stmt = (PyFromImportStatement) parent.getParent();
        final PyReferenceExpression source = stmt.getImportSource();
        if (source == null) return null;
        PsiElement sourceFile = resolveImportReference(source);
        return resolveChild(sourceFile, referencedName, importRef);
      }
    }

    final Module module = ModuleUtil.findModuleForPsiElement(importRef);
    if (module == null) return null;

    RootPolicy<PsiElement> resolvePolicy = new RootPolicy<PsiElement>() {
      public PsiElement visitModuleSourceOrderEntry(final ModuleSourceOrderEntry moduleOrderEntry, final PsiElement value) {
        if (value != null) return value;
        return resolveInRoots(moduleOrderEntry.getRootModel().getContentRoots(), referencedName, importRef);
      }

      public PsiElement visitJdkOrderEntry(final JdkOrderEntry jdkOrderEntry, final PsiElement value) {
        if (value != null) return value;
        return resolveInRoots(jdkOrderEntry.getRootFiles(OrderRootType.SOURCES), referencedName, importRef);
      }
    };
    return ModuleRootManager.getInstance(module).processOrder(resolvePolicy, null);
  }

  @Nullable
  private static PsiElement resolveInRoots(final VirtualFile[] roots, final String referencedName, final PyReferenceExpression importRef) {
    for(VirtualFile contentRoot: roots) {
      PsiElement result = resolveInRoot(contentRoot, referencedName, importRef);
      if (result != null) return result;
    }
    return null;
  }

  @Nullable
  private static PsiElement resolveInRoot(final VirtualFile root, final String referencedName, final PyReferenceExpression importRef) {
    final VirtualFile childFile = root.findChild(referencedName + ".py");
    if (childFile != null) {
      return PsiManager.getInstance(importRef.getProject()).findFile(childFile);
    }

    final VirtualFile childDir = root.findChild(referencedName);
    if (childDir != null) {
      return PsiManager.getInstance(importRef.getProject()).findDirectory(childDir);
    }
    return null;
  }

  @Nullable
  private static PsiElement resolveChild(final PsiElement parent, final String referencedName, final PyReferenceExpression importRef) {
    if (parent instanceof PyFile) {
      return PyResolveUtil.treeWalkUp(new PyResolveUtil.ResolveProcessor(referencedName), parent, null, importRef);
    }
    else if (parent instanceof PsiDirectory) {
      final PsiDirectory dir = (PsiDirectory)parent;
      final PsiFile file = dir.findFile(referencedName + ".py");
      if (file != null) return file;
      return dir.findSubdirectory(referencedName);
    }
    else {
      return null;
    }
  }
}
