package cz.incad.kramerius.rest.api.k5.client.item.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.utils.ChildrenNodeProcessor;
import cz.incad.kramerius.rest.api.k5.client.utils.JSONUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DefaultTreeRenderer implements ItemTreeRender {

	@Inject
	@Named("securedFedoraAccess")
	FedoraAccess fedoraAccess;
	
	@Inject
	SolrAccess solrAccess;
	
	@Inject
	Provider<HttpServletRequest> requestProvider;
	
	public static final int SORTING_KEY = 10000;
	
	@Override
	public boolean isApplicable(String pid, HashMap<String, Object> options) {
		return true;
	}

	@Override
	public int getSortingKey() {
		return SORTING_KEY;
	}

	@Override
	public JSON tree(String pid) {
		try {
			List<ItemTreeNode> nodes = new ArrayList<DefaultTreeRenderer.ItemTreeNode>();
			ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
			for (ObjectPidsPath p : paths) {
				ItemTreeNode node = constructTree(p, pid);
				nodes.add(node);
			}

			if (nodes.size() > 1) {
				for (ItemTreeNode t : nodes) {
					JSONArray jsonArr = new JSONArray();
					jsonArr.add(t.toJSON(this.requestProvider, fedoraAccess));
				}
			} else return nodes.get(0).toJSON(this.requestProvider, fedoraAccess);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private ItemTreeNode constructTree(ObjectPidsPath path, String selectedPid) throws IOException {
        ItemTreeNode node = null;
        States state = States.START;

        while(!path.isEmptyPath()) {
        	state  = state.nextState();
        	
        	String[] pathFromRootToLeaf = path.getPathFromRootToLeaf();
			String pid = pathFromRootToLeaf[path.getLength()-1];

			ItemTreeNode parent = new ItemTreeNode(pid, this.fedoraAccess.getKrameriusModelName(pid));
			if (node != null) parent.addItemTreeNode(node);
        	
			Map<String, Object> options = new HashMap<String, Object>(); {
        		options.put("PID", pid);
        		options.put("NODE", parent);
        		options.put("SELECTED", selectedPid);
			}

			
        	state.processState(this.fedoraAccess, options);
			
			node = parent;
	    	path = path.cutTail(0);
        }
		
		return node;
	}

	
	public static class ItemTreeNode {
		
		private String pid;
		private String model;
		private ItemTreeNode parent;
		private List<ItemTreeNode> children = new ArrayList<ItemTreeNode>();
		private boolean selected;
		
		public ItemTreeNode(String pid, String model) {
			super();
			this.pid = pid;
			this.model = model;
		}
		
		public String getPid() {
			return pid;
		}
		
		public String getModel() {
			return model;
		}
		
		public void addItemTreeNode(ItemTreeNode node) {
			this.children.add(node);
		}
		
		public void removeItemTreeNode(ItemTreeNode node){
			this.children.remove(node);
		}
		
		public List<ItemTreeNode> getItemTreeNode() {
			return this.children;
		}

		public ItemTreeNode getParent() {
			return parent;
		}

		public void setParent(ItemTreeNode parent) {
			this.parent = parent;
		}

		
		
		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(this.pid).append('(').append(model).append(')').append(":");
			builder.append(this.children);
			return builder.toString();
		}

		public JSONObject toJSON(Provider<HttpServletRequest> reqProvider, FedoraAccess fedoraAccess) throws IOException {
			JSONObject jsonObject = JSONUtils.pidAndModelDesc(pid, fedoraAccess, null, null);
			if (isSelected()) {
				jsonObject.put("selected", this.isSelected());
			}
			HttpServletRequest req = reqProvider.get();
			String str = ApplicationURL.applicationURL(req).toString()+"/img?pid="+pid+"&stream=IMG_THUMB&action=GETRAW";
			jsonObject.put("url", str);

			JSONArray jsonArr = new JSONArray();
			for (ItemTreeNode itn : this.children) {
				jsonArr.add(itn.toJSON(reqProvider, fedoraAccess));
			}
			if (jsonArr.size() > 0) {
				jsonObject.put("children", jsonArr);
			}
			
			//jsonObject.put("children", jsonArr);
			
			return jsonObject;
		}
		
		
	}

	
	public  enum States { 

		START {
			@Override
			public States nextState() {
				return States.CHILDREN;
			}

			@Override
			public void processState(FedoraAccess fa, Map<String, Object> options) {
			}
		}, 
		CHILDREN {
			@Override
			public States nextState() {
				return States.SISBS;
			}

			@Override
			public void processState(FedoraAccess fa, Map<String, Object> options) {
				try {
					ItemTreeNode node = (ItemTreeNode) options.get("NODE");
					String pid = options.get("PID").toString();
					if (pid.equals(options.get("SELECTED").toString())) {
						node.setSelected(true);
					}
					ChildrenNodeProcessor processor = new ChildrenNodeProcessor();
					fa.processSubtree(pid, processor);
					List<String> childrenPids = processor.getChildren();
					for (String chPid : childrenPids) {
						ItemTreeNode childNode = new ItemTreeNode(chPid, fa.getKrameriusModelName(chPid));
						node.addItemTreeNode(childNode);
					}
				} catch (ProcessSubtreeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 
		SISBS {
			@Override
			public States nextState() {
				return States.STOP;
			}

			@Override
			public void processState(FedoraAccess fa, Map<String, Object> options) {
				try {
					String pid = options.get("PID").toString();
					ItemTreeNode node = (ItemTreeNode) options.get("NODE");

					if (node.getItemTreeNode().size() == 1) {
						ItemTreeNode oneChild = node.getItemTreeNode().get(0);
						ChildrenNodeProcessor processor = new ChildrenNodeProcessor();
						fa.processSubtree(pid, processor);
						node.removeItemTreeNode(oneChild);
						
						List<String> childrenPids = processor.getChildren();
						for (String chPid : childrenPids) {
							if (chPid.equals(oneChild.getPid())) {
								node.addItemTreeNode(oneChild);
							} else {
								node.addItemTreeNode(new ItemTreeNode(chPid, fa.getKrameriusModelName(chPid)));
							}
						}
						
					}
				} catch (ProcessSubtreeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		},
		STOP {
			@Override
			public States nextState() {
				return States.STOP;
			}

			@Override
			public void processState(FedoraAccess fa, Map<String, Object> options) {
				System.out.println("only stop");
			}
		};
		
		public abstract States nextState();
		
		public abstract void processState(FedoraAccess fa, Map<String, Object> options);
	}

}
