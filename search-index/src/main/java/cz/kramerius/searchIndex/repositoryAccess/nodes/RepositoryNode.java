package cz.kramerius.searchIndex.repositoryAccess.nodes;

import cz.kramerius.shared.AuthorInfo;
import cz.kramerius.shared.DateInfo;
import cz.kramerius.shared.Title;

import java.util.Collections;
import java.util.List;

/**
 * This class contains mostly structural information about single object in repository
 */
public class RepositoryNode {
    //me
    private final String pid;
    private final String model;
    private final Title title;

    //paths
    private final String pidPath;
    private final String modelPath;
    private final List<String> allPidPathsThroughAllParents;

    //root
    private final String rootPid;
    private final String rootModel;
    private final Title rootTitle;
    //own parent
    private final String ownParentPid;
    private final String ownParentModel;
    private final Title ownParentTitle;
    private final Integer positionInOwnParent;

    //foster parents
    private final List<String> pidsOfFosterParents;
    private final List<String> pidsOfFosterParentsOfTypeCollection;
    private final List<String> pidsOfAnyAncestorsOfTypeCollection;
    //children
    private final List<String> pidsOfOwnChildren;
    private final List<String> pidsOfFosterChildren;

    //data from predecessors in the tree and possibly from foster parents' predecessors in their trees
    private final List<String> languages;
    private final List<AuthorInfo> primaryAuthors;
    private final List<AuthorInfo> otherAuthors;
    private final DateInfo dateInfo;
    private final List<String> licenses;

    public RepositoryNode(String pid, String model, Title title,
                          String pidPath, String modelPath, List<String> allPathsToAllParents,
                          String rootPid, String rootModel, Title rootTitle,
                          String ownParentPid, String ownParentModel, Title ownParentTitle, Integer positionInOwnParent,
                          List<String> pidsOfFosterParents, List<String> pidsOfFosterParentsOfTypeCollection, List<String> pidsOfAnyAncestorsOfTypeCollection,
                          List<String> pidsOfOwnChildren, List<String> pidsOfFosterChildren,
                          List<String> languages, List<AuthorInfo> primaryAuthors, List<AuthorInfo> otherAuthors, DateInfo dateInfo,
                          List<String> licenses) {
        this.pid = pid;
        this.model = model;
        this.title = title;
        this.pidPath = pidPath;
        this.modelPath = modelPath;
        this.allPidPathsThroughAllParents = allPathsToAllParents;
        this.rootPid = rootPid;
        this.rootModel = rootModel;
        this.rootTitle = rootTitle;
        this.ownParentPid = ownParentPid;
        this.ownParentModel = ownParentModel;
        this.ownParentTitle = ownParentTitle;
        this.positionInOwnParent = positionInOwnParent;
        this.pidsOfFosterParents = pidsOfFosterParents;
        this.pidsOfFosterParentsOfTypeCollection = pidsOfFosterParentsOfTypeCollection;
        this.pidsOfAnyAncestorsOfTypeCollection = pidsOfAnyAncestorsOfTypeCollection;
        this.pidsOfOwnChildren = pidsOfOwnChildren;
        this.pidsOfFosterChildren = pidsOfFosterChildren;
        this.languages = languages;
        this.primaryAuthors = primaryAuthors;
        this.otherAuthors = otherAuthors;
        this.dateInfo = dateInfo;
        this.licenses = licenses;
    }

    public String getPid() {
        return pid;
    }

    public String getModel() {
        return model;
    }

    public Title getTitle() {
        return title;
    }

    public String getPidPath() {
        return pidPath;
    }

    public String getModelPath() {
        return modelPath;
    }

    public List<String> getAllPidPathsThroughAllParents() {
        if (allPidPathsThroughAllParents == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(allPidPathsThroughAllParents);
        }
    }

    public String getRootPid() {
        return rootPid;
    }

    public String getRootModel() {
        return rootModel;
    }

    public Title getRootTitle() {
        return rootTitle;
    }

    public String getOwnParentPid() {
        return ownParentPid;
    }

    public String getOwnParentModel() {
        return ownParentModel;
    }

    public Title getOwnParentTitle() {
        return ownParentTitle;
    }

    public Integer getPositionInOwnParent() {
        return positionInOwnParent;
    }

    public List<String> getPidsOfFosterParents() {
        if (pidsOfFosterParents == null) {
            return null;
        } else {
            return Collections.unmodifiableList(pidsOfFosterParents);
        }
    }

    public List<String> getPidsOfFosterParentsOfTypeCollection() {
        if (pidsOfFosterParentsOfTypeCollection == null) {
            return null;
        } else {
            return Collections.unmodifiableList(pidsOfFosterParentsOfTypeCollection);
        }
    }

    public List<String> getPidsOfAnyAncestorsOfTypeCollection() {
        if (pidsOfAnyAncestorsOfTypeCollection == null) {
            return null;
        } else {
            return Collections.unmodifiableList(pidsOfAnyAncestorsOfTypeCollection);
        }
    }

    public List<String> getPidsOfOwnChildren() {
        if (pidsOfOwnChildren == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(pidsOfOwnChildren);
        }
    }

    public List<String> getPidsOfFosterChildren() {
        if (pidsOfFosterChildren == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(pidsOfFosterChildren);
        }
    }

    public List<String> getLanguages() {
        return languages == null ? Collections.emptyList() : languages;
    }

    public List<AuthorInfo> getPrimaryAuthors() {
        return primaryAuthors == null ? Collections.emptyList() : primaryAuthors;
    }

    public List<AuthorInfo> getOtherAuthors() {
        return otherAuthors == null ? Collections.emptyList() : otherAuthors;
    }

    public DateInfo getDateInfo() {
        return dateInfo;
    }

    public List<String> getLicenses() {
        return licenses == null ? Collections.emptyList() : licenses;
    }
}
